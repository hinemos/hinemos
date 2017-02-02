/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.systemlog.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.systemlog.bean.SyslogMessage;
import com.clustercontrol.util.HinemosTime;

public class SyslogReceiver {

	private static final Log log = LogFactory.getLog(SyslogReceiver.class);

	public final String listenAddress;
	public final int listenPort;
	public final Charset charset;

	private DatagramSocket socket;
	private int socketBufferSize = 8388608;
	private int socketTimeout = 1000; //milisecond

	private final SyslogHandler _handler;

	private ExecutorService _executor;

	public boolean shutdown = false;

	public SyslogReceiver(String listenAddress, int listenPort, Charset charset, SyslogHandler handler) {
		this.listenAddress = listenAddress;
		this.listenPort = listenPort;
		this.charset = charset;

		this._handler = handler;
	}

	public void setSocketBufferSize(int size) {
		socketBufferSize = size;
	}

	public int getSocketBufferSize() {
		return socketBufferSize;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public synchronized void start() throws SocketException, UnknownHostException {
		log.info(String.format("starting SyslogReceiver. [address = %s, port = %s, charset = %s, handler = %s]", listenAddress, listenPort, charset.name(), _handler.getClass().getName()));
		
		// リソースを生成したのち、バックエンドとなるhandler, receiver, socketの順に開始します
		
		// クラスタ構成ではHinemosマネージャにてListenしない
		if (! HinemosManagerMain._isClustered) {
			socket = new DatagramSocket(listenPort, InetAddress.getByName(listenAddress));
			socket.setReceiveBufferSize(socketBufferSize);
			socket.setSoTimeout(socketTimeout);
		}
		
		_executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "SystemLogReceiver");
			}
		});

		_handler.start();
		
		if (! HinemosManagerMain._isClustered) {
			_executor.submit(new ReceiverTask(socket, _handler));
		}
	}

	public synchronized void shutdown() {
		log.info(String.format("stopping SyslogReceiver. [address = %s, port = %s, charset = %s, handler = %s]", listenAddress, listenPort, charset.name(), _handler.getClass().getName()));
		
		// frontendとなるsocket, receiver, handlerの順で停止していく
		shutdown = true;
		
		if (! HinemosManagerMain._isClustered) {
			socket.close();
		}
		
		_executor.shutdown();
		_handler.shutdown();
	}

	private class ReceiverTask implements Runnable {
		public final long _sleepMsec = 1000;
		public final int _bufferSize = 8192;

		private final byte[] buffer = new byte[_bufferSize];
		
		public DatagramSocket socket;
		public SyslogHandler _handler;
		
		public ReceiverTask(DatagramSocket socket, SyslogHandler _handler) {
			this.socket = socket;
			this._handler = _handler;
		}

		@Override
		public void run() {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			long lastSentTime = -1;
			ArrayList<SyslogMessage> syslogList = new ArrayList<SyslogMessage>();
			while (! shutdown) {
				try {
					socket.receive(packet);
					
					byte[] syslogRaw = Arrays.copyOf(packet.getData(), packet.getLength());

					SyslogMessage syslog = byteToSyslog(syslogRaw);
					syslogList.add(syslog);
					
					if (log.isDebugEnabled()) {
						log.debug(String.format("syslog received : %s", syslog.rawSyslog));
					}
					
					long now = HinemosTime.currentTimeMillis();
					if (now - lastSentTime > 1000) {
						_handler.syslogReceived(syslogList);
						syslogList = new ArrayList<SyslogMessage>();
						lastSentTime = now;
					}
					
				} catch (SocketTimeoutException e) {
					if (!syslogList.isEmpty()) {
						_handler.syslogReceived(syslogList);
						syslogList = new ArrayList<SyslogMessage>();
						lastSentTime = HinemosTime.currentTimeMillis();
					}
				} catch (Exception e) {
					if (e instanceof SocketException) {
						if (shutdown) {
							continue;	// if shutdown
						}
					}

					log.warn("syslog receiver failure.", e);
					try {
						Thread.sleep(_sleepMsec);
					} catch (Exception sleepEx) { }
				}
			}
			
			if (!syslogList.isEmpty()) {
				_handler.syslogReceived(syslogList);
			}
		}
	}
	
	public SyslogMessage byteToSyslog(byte[] syslogRaw) throws ParseException, HinemosUnknown {
		String syslog = new String(syslogRaw, 0, syslogRaw.length, charset);
		return SyslogMessage.parse(syslog);
	}
	
}
