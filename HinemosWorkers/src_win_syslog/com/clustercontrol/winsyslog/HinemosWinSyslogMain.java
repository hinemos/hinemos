/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winsyslog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.winsyslog.SyslogUdpReceiver.UdpSocketDecorator;

public class HinemosWinSyslogMain {
	private static Log log = LogFactory.getLog(HinemosWinSyslogMain.class);
	private static final int waitForTermination = 60 * 1000;
	private static final Object shutdownLock = new Object();
	private static boolean shutdown = false;

	public static void main(String[] args) {
		log.info("start WinSyslog.");
		
		// プロパティファイル読み込み
		{
			String etcDir = System.getProperty("hinemos.manager.etc.dir", "./etc");
			String configFilePath = new File(etcDir, "syslog.conf").getAbsolutePath();
			
			log.debug("init() : propFileName = " + configFilePath);
			try (FileInputStream inputStream = new FileInputStream(configFilePath)) {
				Properties properties = new Properties();
				properties.load(inputStream);
				WinSyslogConfig.setProperties(properties);
			} catch (FileNotFoundException e) {
				log.warn(e.getMessage());
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
			}
		}
		
		try {
			for (WinSyslogConfig v: WinSyslogConfig.values()) {
				log.info(v.toString());
			}
			
			final HinemosWinSyslogMain winSyslog = new HinemosWinSyslogMain();
			winSyslog.start();
			
			final CountDownLatch sync = new CountDownLatch(1);
			
			Runtime.getRuntime().addShutdownHook(
					new Thread() {
						@Override
						public void run() {
							log.info("call shutdown-hook.");
							
							synchronized(shutdownLock) {
								shutdown = true;
								shutdownLock.notify();
								
								winSyslog.stop();
							}
							
							try {
								sync.await();
							} catch (InterruptedException e) {
							}
						}
					});
			
			synchronized(shutdownLock) {
				while (!shutdown) {
					try {
						shutdownLock.wait();
					} catch (InterruptedException e) {
						log.warn("shutdown lock interrupted.", e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException sleepE) { };
					}
				}
			}
			
			winSyslog.join();
			
			log.info("stopped WinSyslog.");
			
			sync.countDown();
			
			System.exit(0);
		} catch(RuntimeException | IOException e) {
			log.warn(e.getMessage(), e);
			System.exit(1);
		}
	}
	
	private SyslogTcpReceiver tcpReceiver;
	private SyslogUdpReceiver udpReceiver;
	private UdpSender sender;
	private ThreadPoolExecutor worker;
	
	public void start(Properties properties) throws IOException {
		WinSyslogConfig.setProperties(properties);
		start();
	}
	
	public void start() throws IOException {
		// 論理 CPU 数 でプールを作成。
		// pool サイズが 論理 CPU 数 より大きくなると、性能が劣化する。
		int procNum = Runtime.getRuntime().availableProcessors();
		log.info(String.format("number of processor = %d", procNum));
		
		int threadNum = WinSyslogConfig.worker_thread_max_size.value(Math.max(procNum, 1));
		log.info(String.format("thread pool : coreSize=%d, maxSize=%d", procNum, Math.max(procNum, threadNum)));
		
		// スレッドプール作成。
		// java 既定の動作だと、コアサイズより多いリクエストは、先にキューに格納されるので、
		// 先に最大サイズまでスレッドを消費するよう以下で動作を修正。最大スレッドを超えるリクエストは、キューに保存。
		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>() {
			private static final long serialVersionUID = -6903933921423432194L;
			@Override
			public boolean offer(Runnable e) {
				// 原則、false を返すように変更。
				// この関数で false を返すと、スレッドプールは、キューにタスクを追加できない。
				if (size() == 0) {
					return super.offer(e);
				} else {
					return false;
				}
			}
		};
		
		worker = new ThreadPoolExecutor(procNum, Math.max(procNum, threadNum),
				WinSyslogConfig.worker_thread_keepalive_timeout.<Integer>value(), TimeUnit.MILLISECONDS,
				queue,
				new ThreadFactory() {
					private AtomicInteger index = new AtomicInteger();
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format("Thread-SyslogWorker-%d", index.incrementAndGet()));
					}
				}) {
					@Override
					protected void terminated() {
						super.terminated();
						sender.close();
					}
			};
		
		worker.setRejectedExecutionHandler(new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					try {
						// キューの修正により、追加できなかったタスクを以下で追加。
						executor.getQueue().put(r);
					} catch (InterruptedException e) {
						log.warn("accept() : rejected from worker-thread-pool. " + e.getMessage());
					}
				}
			});
		
		// メッセージ用文字セット
		Charset debugCharset = Charset.forName(WinSyslogConfig.debug_charset.<String>value());
		
		SyslogPacketHelper parser = new SyslogPacketHelper(debugCharset);
		
		// UDP 送信用ハンドラー作成。
		String targetValue = WinSyslogConfig.send_targets.value();
		log.info("UdpSender initialize." + " (target=" + targetValue + ")");
		sender = UdpSender.build(targetValue);
		sender.setSyslogPacketParser(parser);
		
		// 受信ポート取得。
		int port = WinSyslogConfig.receive_port.value();
		
		// TCP 受信用オブジェクト作成
		if (WinSyslogConfig.receive_tcp.value()) {
			Counter counter = new Counter("The number of received TCP syslog");
			tcpReceiver = SyslogTcpReceiver.build("0.0.0.0", port, counter, new ResponseHandler<byte[]>() {
				@Override
				public void accept(byte[] message) {
					sender.send(message);
				}
				@Override
				public String toString() {
					return "TcpResponseHandler []";
				}
			}, worker);
			
			// セパレーターを取得。
			String[] sepString = WinSyslogConfig.split_code.<String>value().split(",");
			final List<Byte> seps = new ArrayList<>();
			for (String s: sepString) {
				seps.add(Byte.parseByte(s));
			}
			tcpReceiver.setDelimiteres(seps);
			
			// 読み取りのタイムアウトを設定
			tcpReceiver.setReadTimeout(WinSyslogConfig.receive_tcp_read_timeout.<Integer>value());
			
			// 読み取り最大サイズを設定
			String maxSizeStr = WinSyslogConfig.receive_tcp_read_max_size.value();
			int maxSize;
			if (maxSizeStr.endsWith("K") || maxSizeStr.endsWith("k")) {
				maxSize = Integer.parseInt(maxSizeStr.substring(0, maxSizeStr.length() - 1)) * 1024;
			} else {
				maxSize = Integer.parseInt(maxSizeStr);
			}
			tcpReceiver.setMaxReadSize(maxSize);
			
			tcpReceiver.setSyslogPacketParser(parser);
			
			// tcp レシーバー開始。
			log.info("TcpReceiver is starting.");
			tcpReceiver.start();
		} else {
			tcpReceiver = null;
		}
		
		// UDP 受信用オブジェクト作成
		if (WinSyslogConfig.receive_udp.value()) {
			Counter counter = new Counter("The number of received UDP syslog");
			// 受信内容の処理方法を決定。
			udpReceiver = SyslogUdpReceiver.build("0.0.0.0", port, counter, new ResponseHandler<byte[]>() {
				public void accept(byte[] message) {
					sender.send(message);
				}
				@Override
				public String toString() {
					return "UdpResponseHandler []";
				}
			}, worker);
			
			// UDP の受信バッファーサイズを変更して、ドロップ率を調整。
			udpReceiver.setServerDecorator(new UdpSocketDecorator() {
				@Override
				public void accept(DatagramSocket server) throws IOException {
					server.setReceiveBufferSize(WinSyslogConfig.receive_udp_buffer.value(server.getReceiveBufferSize()));
				}
			});
			
			udpReceiver.setParser(parser);
			
			// UDP レシーバー開始。
			log.info("UdpReceiver is starting.");
			udpReceiver.start();
		} else {
			udpReceiver = null;
		}
	}
	
	public void stop() {
		if (tcpReceiver != null) {
			tcpReceiver.stop();
		}
		
		if (udpReceiver != null) {
			udpReceiver.stop();
		}
		
		worker.shutdown();
	}
	
	public void join() {
		if (tcpReceiver != null) {
			try {
				tcpReceiver.join(waitForTermination);
				log.info("joined TcpReceiver.");
			} catch(InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		}
		
		if (udpReceiver != null) {
			try {
				udpReceiver.join(waitForTermination);
				log.info("joined UdpReceiver.");
			} catch(InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		}
		
		try {
			worker.awaitTermination(waitForTermination, TimeUnit.MILLISECONDS);
			log.info("terminated thread-pool.");
		} catch(InterruptedException e) {
			log.warn(e.getMessage(), e);
		}
	}
}