/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.service;


import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.systemlog.util.Counter;
import com.clustercontrol.systemlog.util.ResponseHandler;
import com.clustercontrol.systemlog.util.SyslogHandler;
import com.clustercontrol.systemlog.util.SyslogPacketHelper;
import com.clustercontrol.systemlog.util.SyslogTcpReceiver;
import com.clustercontrol.systemlog.util.SyslogUdpReceiver;
import com.clustercontrol.systemlog.util.SyslogUdpReceiver.UdpSocketDecorator;

public class SyslogService {
	private static Log log = LogFactory.getLog(SyslogService.class);
	private static final int waitForTermination = 60 * 1000;
	
	private SyslogTcpReceiver tcpReceiver;
	private SyslogUdpReceiver udpReceiver;
	
	public void start(SyslogHandler handler) throws IOException {
		// 論理 CPU 数 でプールを作成。
		// pool サイズが 論理 CPU 数 より大きくなると、性能が劣化する。
		int procNum = Runtime.getRuntime().availableProcessors();
		log.info(String.format("number of processor = %d", procNum));
		Long defaultThreadNum = Long.valueOf(Math.max(procNum, 1));
		int threadNum = HinemosPropertyCommon.monitor_systemlog_worker_thread_max_size.getIntegerValue("", defaultThreadNum);
		log.info(String.format("thread pool : coreSize=%d, maxSize=%d", procNum, Math.max(procNum, threadNum)));
		//SyslogReceiver用の各種プロパティを取得
		int maxConnSize = HinemosPropertyCommon.monitor_systemlog_receive_tcp_max_connection.getIntegerValue();
		boolean tcpKeepAlive = HinemosPropertyCommon.monitor_systemlog_receive_tcp_keepAlive.getBooleanValue();
		int tcpBacklog = HinemosPropertyCommon.monitor_systemlog_receive_tcp_backlog.getIntegerValue();
		boolean enableReadTimeout = HinemosPropertyCommon.monitor_systemlog_receive_tcp_read_timeout_enable.getBooleanValue();
		long keepAliveTime = HinemosPropertyCommon.monitor_sytemslog_worker_thread_keepalive_timeout.getNumericValue();
		int recvQueueSize = HinemosPropertyCommon.monitor_systemlog_filter_queue_size.getIntegerValue();
		
		// ヘッダー用文字セット
		Charset headerCharset = Charset.forName(HinemosPropertyCommon.monitor_systemlog_header_charset.getStringValue());
		
		// メッセージ用文字セット
		Charset messageCharset = Charset.forName(HinemosPropertyCommon.monitor_systemlog_message_charset.getStringValue());
		
		SyslogPacketHelper parser = new SyslogPacketHelper(headerCharset, messageCharset);
		
		
		// 受信ポート取得。
		int port = HinemosPropertyCommon.monitor_systemlog_listen_port.getIntegerValue();
		String address = HinemosPropertyCommon.monitor_systemlog_listen_address.getStringValue();
		
		// TCP 受信用オブジェクト作成
		if (HinemosPropertyCommon.monitor_systemlog_listen_tcp.getBooleanValue()) {
			Counter counter = new Counter("The number of received TCP syslog");
			@SuppressWarnings("unchecked")
			ResponseHandler<byte[]> _handler = (ResponseHandler<byte[]>)handler;
			tcpReceiver = SyslogTcpReceiver.build(address, port, counter, _handler, maxConnSize, threadNum, tcpKeepAlive, tcpBacklog);

			
			// セパレーターを取得。
			String[] sepString = HinemosPropertyCommon.monitor_systemlog_split_code.getStringValue().split(",");
			final List<Byte> seps = new ArrayList<>();
			for (String s: sepString) {
				seps.add(Byte.parseByte(s));
			}
			tcpReceiver.setDelimiteres(seps);
			
			// 読み取りのタイムアウトを設定
			tcpReceiver.enableReadTimeout(enableReadTimeout);
			tcpReceiver.setReadTimeout(HinemosPropertyCommon.monitor_systemlog_receive_tcp_read_timeout.getIntegerValue());
			
			// 読み取り最大サイズを設定
			String maxSizeStr = HinemosPropertyCommon.monitor_systemlog_receive_tcp_read_max_size.getStringValue();
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
		if (HinemosPropertyCommon.monitor_systemlog_listen_udp.getBooleanValue()) {
			Counter counter = new Counter("The number of received UDP syslog");
			@SuppressWarnings("unchecked")
			ResponseHandler<byte[]> _handler = (ResponseHandler<byte[]>)handler;
			// 受信内容の処理方法を決定。
			udpReceiver = SyslogUdpReceiver.build(address, port, counter, _handler, recvQueueSize, threadNum, keepAliveTime);
			
			// UDP の受信バッファーサイズを変更して、ドロップ率を調整。
			udpReceiver.setServerDecorator(new UdpSocketDecorator() {
				@Override
				public void accept(DatagramSocket server) throws IOException {
					Long receivedBufferSize = Long.valueOf(server.getReceiveBufferSize());
					server.setReceiveBufferSize(HinemosPropertyCommon.monitor_systemlog_receive_udp_buffer.getIntegerValue("",receivedBufferSize));
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
		
	}
}