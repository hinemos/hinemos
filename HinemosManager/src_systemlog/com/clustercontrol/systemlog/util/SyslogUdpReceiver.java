/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/*
 * Udp 受信用クラス。
 */
public class SyslogUdpReceiver {
	private static Logger logger = Logger.getLogger(SyslogUdpReceiver.class);

	// 受信数のカウンタ
	private Counter counter;

	// 読込用の一時バッファーサイズ(8k)
	private static final int READ_BUFFER_SIZE = 8192;
	
	// メイン処理用スレッド。
	private Thread mainThread;
	
	// リッスン用
	private String address;
	private int port;
	
	// 読み取り時のブロックのタイムアウト。
	private static int RECIEVE_TOMEOUT = 1000;
	
	private int recvQueueSize;
	
	// ハンドラーを実行するスレッドプール。
	private ThreadPoolExecutor worker;
	private int threadNum;
	private long keepAliveTime;
	
	// 読み込んだ情報を処理するハンドラー。
	private ResponseHandler<byte[]> handler;
	
	private CountDownLatch sync = new CountDownLatch(1);
	
	private SyslogPacketHelper parser = new SyslogPacketHelper();
	
	// Udp ソケットの初期値変更用
	public interface UdpSocketDecorator {
		void accept(DatagramSocket socket) throws IOException;
	}
	private UdpSocketDecorator decorator = new UdpSocketDecorator() {
		@Override
		public void accept(DatagramSocket socket) throws IOException {
		}
	};
	
	// ループの継続判定用。
	private boolean loop = true;
	
	private class RecieveTask implements Runnable {
		private DatagramSocket server;
		
		public RecieveTask(DatagramSocket server) {
			this.server = server;
		}
		
		@Override
		public void run() {
			sync.countDown();

			logger.info(String.format("listening() : start listening syslog packet using udp. address=%s, port=%d", address, port));

			try (DatagramSocket closable = server) {
				// 定期的にタイムアウトさせて、処理の終了確認をさせる。
				server.setSoTimeout(RECIEVE_TOMEOUT);
				
				// ソケットの初期値を変更。
				decorator.accept(server);
				
				byte[] buffer = new byte[READ_BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				while (loop) {
					byte[] message;
					try {
						server.receive(packet);
						if (logger.isTraceEnabled())
							logger.trace(String.format("listen() : recieved. %s", packet.getAddress()));
						
						message = Arrays.copyOf(packet.getData(), packet.getLength());
						counter.incrementSuccess();
					} catch (SocketTimeoutException e) {
						// 受信したカウントを出力
						if (logger.isDebugEnabled()) {
							if ((System.currentTimeMillis() / 1000) % 30 == 0) {
								logger.debug(counter.toString());
							}
						}
						continue;
					} catch (IOException e) {
						counter.incrementFailed();
						logger.warn(e.getMessage(), e);
						try {
							Thread.sleep(1000);
						} catch(InterruptedException e1) {
						}
						continue;
					}
					
					if (logger.isTraceEnabled()) {
						try {
							String[] result = parser.splitSyslogMessage(message);
							if (result != null) {
								logger.trace(String.format("read() : %s, header=%s, message=%s", packet.getAddress(), result[0], result[1]));
							} else {
								logger.warn(String.format("read() : %s, string=%s, array=%s", packet.getAddress(), new String(message, parser.getHdrCharset()), Arrays.toString(message)));
							}
						} catch(IOException e) {
							logger.warn(e.getMessage(), e);
						}
					}
					
					final byte[] m = message;
					//packetクラスのまま別スレッドに引き渡すと、実行速度に影響するのでfinal指定で変数化
					final String i = packet.getAddress().toString(); 
					Runnable sender = new Runnable() {
						@Override
						public void run() {
							handler.accept(m,i);
						}
					};
					//workerのキューサイズがmonitor_systemlog_filter_queue_sizeを超えたらドロップ
					try {
						if(worker.getQueue().size()>= recvQueueSize){
							logger.warn("read(): Exceeded max queue size. Drop message: "+new String(m)+" [Sender]"+i);
						}
						else{
							worker.execute(sender);
						}
					} catch(RejectedExecutionException e) {
						logger.warn("accept() : rejected from worker-thread-pool. " + e.getMessage() + " " + handler.toString());
					}
				}
			} catch(RuntimeException | IOException e) {
				logger.warn("Faile to start SyslogTcpReceiver. " + SyslogUdpReceiver.this.toString(), e);
			}
			logger.info(String.format("listen() : stopped. %s", SyslogUdpReceiver.this.toString()));
		}
	};
	
	public SyslogUdpReceiver(String address, int port, Counter counter, ResponseHandler<byte[]> handler, int recvQueueSize, int threadNum, long keepAliveTime) {
		this.counter = counter;
		this.address = address;
		this.port = port;
		this.handler = handler;
		this.recvQueueSize = recvQueueSize;
		this.threadNum = threadNum;
		this.keepAliveTime = keepAliveTime;
	}
	
	public void start() throws SocketException {
		if (mainThread != null)
			logger.warn("start() : already started. " + this.toString());
		
		DatagramSocket server = new DatagramSocket(new InetSocketAddress(address, port));
		
		// スレッドプール作成。
		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>() {
			private static final long serialVersionUID = -6903933921423432194L;
		};
		worker = new ThreadPoolExecutor(threadNum, threadNum, keepAliveTime, TimeUnit.MILLISECONDS,
				queue, new ThreadFactory() {
					private AtomicInteger index = new AtomicInteger();

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format("Thread-SyslogUdpWorker-%d", index.incrementAndGet()));
					}
		});
		
		
		mainThread = new Thread(new RecieveTask(server), "Thread-SyslogUdpReceiver");
		
		mainThread.start();
			
		try {
			sync.await();
		} catch (InterruptedException e) {
		}
	}
	
	public void stop() {
		loop = false;
		
		if (worker != null) {
			worker.shutdown();
		}
		logger.info(String.format("stop() : stopping. %s", this.toString()));
	}
	
	public void join(long wait) throws InterruptedException {
		mainThread.join(wait);
	}
	
	public void setParser(SyslogPacketHelper parser) {
		this.parser = parser;
	}
	
	public SyslogUdpReceiver setServerDecorator(UdpSocketDecorator decorator) {
		this.decorator = decorator;
		return this;
	}
	
	public static SyslogUdpReceiver build(String address, int port, Counter counter, ResponseHandler<byte[]> handler, int recvQueueSize, int threadNum, long keepAliveTime) {
		return new SyslogUdpReceiver(address, port, counter, handler, recvQueueSize, threadNum, keepAliveTime);
	}

	@Override
	public String toString() {
		return "SyslogUdpReceiver [address=" + address + ", port=" + port + ", loop=" + loop + "]";
	}
}