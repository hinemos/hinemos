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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/*
 * レシーバが受信したシステムログをUDPで送信するクラス
 */
public class UdpSender {
	private static Logger logger = Logger.getLogger(UdpSender.class);
	
	private Counter counter = new Counter("The number of sended syslog");

	private DatagramSocket socket;
	private List<InetSocketAddress> addresses;
	
	// 最大リトライ数
	private final static int MAX_RETRY_COUNT = 10;
	
	private SyslogPacketHelper parser = new SyslogPacketHelper();
	
	public UdpSender(List<InetSocketAddress> addresses) throws SocketException {
		this.addresses = Collections.unmodifiableList(new ArrayList<>(addresses));
		this.socket = new DatagramSocket();
	}
	
	/*
	 * 複数のターゲットへ送信。
	 */
	public void send(byte[] message) {
		if (logger.isTraceEnabled()) {
			String[] result;
			try {
				result = parser.splitSyslogMessage(message);
				if (result != null) {
					logger.trace(String.format("send() : %s, header=%s, message=%s", addresses, result[0], result[1]));
				} else {
					logger.warn(String.format("send() : %s, string=%s, array=%s", addresses, new String(message, parser.getHdrCharset()), Arrays.toString(message)));
				}
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		
		int count = 0;
		
		List<InetSocketAddress> targets = new ArrayList<>(addresses);
		while(!targets.isEmpty() && count < MAX_RETRY_COUNT) {
			// 送信
			Iterator<InetSocketAddress> iter = targets.iterator();
			while(iter.hasNext()) {
				InetSocketAddress addr = iter.next();
				
				try {
					if (logger.isTraceEnabled())
						logger.trace("send() : send to [" + addr + "]");
					DatagramPacket packet =  new DatagramPacket(message, message.length, addr);
					socket.send(packet);
					counter.incrementSuccess();
					iter.remove();
				} catch (IOException e) {
					logger.warn(e.getMessage() + " : " + addr, e);
				}
			}
			
			++count;
			
			if (!targets.isEmpty()) {
				if (logger.isDebugEnabled())
					logger.debug(String.format("send() : wait to retry. count=%d", count));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
		
		for (int i = 0; i < targets.size(); ++i) {
			counter.incrementFailed();
		}
	}
	
	public static UdpSender build(String targets) throws SocketException {
		// 設定値を分解する
		// ex)192.168.0.1:24001,192.168.0.2:24001
		List<InetSocketAddress> sockets = new ArrayList<>();
		if (targets != null && targets.length() > 0) {
			String[] targetsArray = targets.split(",");
			for (String target : targetsArray) {
				String[] address = target.split(":");
				if (address.length != 2) {
					logger.warn("build() : illegal configuration value line \"syslog.send.targets\":" + target);
					continue;
				}
				
				try {
					InetSocketAddress socketAddress = new InetSocketAddress(address[0].trim(), Integer.parseInt(address[1].trim()));
					sockets.add(socketAddress);
				} catch (Exception e) {
					logger.warn("build() : illegal configuration value line \"syslog.send.targets\":" + target, e);
				}
			}
			
		} else {
			logger.warn("build() : configuration value \"syslog.send.targets\" is not defined.");
		}
		return build(sockets);
	}
	
	public static UdpSender build(List<InetSocketAddress> addresses) throws SocketException {
		return new UdpSender(addresses);
	}
	
	public void setSyslogPacketParser(SyslogPacketHelper parser) {
		this.parser = parser;
	}
	
	public void close() {
		socket.close();
	}
}