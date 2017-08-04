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

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SyslogReceiver {
	private static Log log = LogFactory.getLog(SyslogReceiver.class);
	private static Log receiveLog = LogFactory.getLog("Receive");
	
	private static final long TASK_SLEEP = 1000;
	private static final int READ_BUFFER_SIZE = 8196;
	
	private final boolean tcpEnable;
	private final boolean udpEnable;
	private final int listenPort;
	
	private boolean shutdown = false;
	
	// TCP
	private ExecutorService tcpExecutor;
	private ServerSocket tcpSocket;
	
	// UDP
	private ExecutorService udpExecutor;
	private DatagramSocket udpSocket;

	public SyslogReceiver(boolean tcpEnable, boolean udpEnable, int listenPort) {
		this.tcpEnable = tcpEnable;
		this.udpEnable = udpEnable;
		this.listenPort = listenPort;
	}
	
	public synchronized void start() throws SocketException, UnknownHostException, IOException {
		
		if (tcpEnable) {
			tcpSocket = new ServerSocket(listenPort);
			tcpExecutor = createExecutorService("TcpReceiver");
			tcpExecutor.submit(new TcpReceiverTask(tcpSocket));
		}
		
		if (udpEnable) {
			udpSocket = new DatagramSocket(listenPort);
			udpExecutor = createExecutorService("UdpReceiver");
			udpExecutor.submit(new UdpReceiverTask(udpSocket));
		}
		
		if (!tcpEnable && !udpEnable) {
			log.warn("Both TCP and UDP are disabled.");
		}
	}
	
	private ExecutorService createExecutorService(final String name) {
		ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, name);
			}
		});
		return executor;
	}
	
	public synchronized void shutdown() {
		shutdown = true;
		
		if (tcpEnable) {
			try {
				tcpSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tcpExecutor.shutdown();
		}
		
		if (udpEnable) {
			udpSocket.close();
			udpExecutor.shutdown();
		}
	}
	
	private class TcpReceiverTask implements Runnable {
		public ServerSocket socket;
		
		public TcpReceiverTask(ServerSocket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[READ_BUFFER_SIZE];
			Socket clientSocket = null;
			while (!shutdown) {
				try {
					clientSocket = socket.accept();
					InputStream inStream = clientSocket.getInputStream();
					int readPos = 0;

					while ( readPos < 1 ) {
						int inputLen = inStream.read( buffer, readPos, READ_BUFFER_SIZE - readPos );
						readPos += inputLen;
					}

					byte[] sendData = Arrays.copyOfRange( buffer, 0, readPos );
					UdpSender.send(sendData);
					
					String receiveText = new String(sendData, "UTF-8");
							
					log.debug("Receive [TCP]:" + receiveText);
					receiveLog.debug(receiveText);
					
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				} catch (Exception e) {
					if (e instanceof SocketException) {
						if (shutdown) {
							continue;
						}
					}

					try {
						Thread.sleep(TASK_SLEEP);
					} catch (Exception e2) {}
				} finally {
					if (clientSocket != null) {
						try {
							clientSocket.close();
						} catch (IOException e) {}
					}
				}
			}
		}
	}
	
	private class UdpReceiverTask implements Runnable {
		public DatagramSocket socket;
		
		public UdpReceiverTask(DatagramSocket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[READ_BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			
			while (!shutdown) {
				try {
					socket.receive(packet);
					
					byte[] sendData = Arrays.copyOf(packet.getData(), packet.getLength());
					UdpSender.send(sendData);
					
					String receiveText = new String(sendData, "UTF-8");
					
					log.debug("Receive [UDP]:" + receiveText);
					receiveLog.debug(receiveText);
					
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				} catch (Exception e) {
					if (e instanceof SocketException) {
						if (shutdown) {
							continue;
						}
					}

					try {
						Thread.sleep(TASK_SLEEP);
					} catch (Exception e2) {}
				}
			}
		}
	}
}
