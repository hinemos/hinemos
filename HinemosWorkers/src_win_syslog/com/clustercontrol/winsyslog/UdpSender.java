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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * レシーバが受信したシステムログをUDPで送信するクラス
 */
public class UdpSender {
	private static Log log = LogFactory.getLog(UdpSender.class);
	
	private static InetSocketAddress[] toAddresses = null;
	
	/**
	 * 初期化処理
	 * @param configValue 送信先の設定値
	 */
	public static void init(String configValue) {
		// 設定値を分解する
		// ex)192.168.0.1:24001,192.168.0.2:24001
		List<InetSocketAddress> sockets = new ArrayList<InetSocketAddress>();
		if (configValue != null && configValue.length() > 0) {
			String[] targetsArray = configValue.split(",");
			for (String target : targetsArray) {
				String[] address = target.split(":");
				if (address.length != 2) {
					log.warn("illegal configuration value line \"syslog.send.targets\":" + target);
					continue;
				}
				
				try {
					InetSocketAddress socketAddress = new InetSocketAddress(address[0].trim(), Integer.parseInt(address[1].trim()));
					sockets.add(socketAddress);
				} catch (Exception e) {
					log.warn("illegal configuration value line \"syslog.send.targets\":" + target, e);
				}
			}
			
		} else {
			log.warn("configuration value \"syslog.send.targets\" is not defined.");
		}
		toAddresses = sockets.toArray(new InetSocketAddress[0]);
	}
	
	/**
	 * データを送信する
	 * @param sendBuff 送信データ
	 */
	public static void send(byte[] sendBuff) {
		if (toAddresses == null) {
			log.warn("UdpSender is not Initialised.");
			return;
		} else if (toAddresses.length == 0) {
			log.warn("send target is none.");
			return;
		}
		
		SenderTask sender = new SenderTask(sendBuff);
		Thread thread = new Thread(sender);
		thread.start();
	}
	
	private static class SenderTask implements Runnable {
		private byte[] sendBuff;
		
		public SenderTask(byte[] sendBuff) {
			this.sendBuff = sendBuff;
		}
		
		@Override
		public void run() {
			try {
				for (InetSocketAddress addr : toAddresses) {
					log.debug("send to [" + addr.toString() + "]");
					DatagramSocket dataSocket = new DatagramSocket();
					DatagramPacket packet =  new DatagramPacket(sendBuff, sendBuff.length, addr);
					dataSocket.send(packet);
					dataSocket.close();
				}
				
			} catch (SocketException e) {
				log.error("send failed." , e);
			} catch (IOException e) {
				log.error("send failed." , e);
			}
		}
	}
	
}
