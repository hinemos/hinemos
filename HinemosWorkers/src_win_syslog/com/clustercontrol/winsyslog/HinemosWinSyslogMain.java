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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HinemosWinSyslogMain {
	private static Log log = LogFactory.getLog(HinemosWinSyslogMain.class);
	
	// shutdownHookが呼ばれるまでmainスレッドを待機させるためのLockオブジェクトおよびフラグ
	private static final Object shutdownLock = new Object();
	private static boolean shutdown = false;

	private static SyslogReceiver receiver;

	public static void main(String[] args) {
		try {
			log.info("receiver started.");
			
			// プロパティファイル読み込み
			String etcDir = System.getProperty("hinemos.manager.etc.dir");
			String configFilePath = new File(etcDir, "syslog.conf").getAbsolutePath();
			WinSyslogConfig.init(configFilePath);
			
			// Sender初期化
			String targetValue = WinSyslogConfig.getProperty("syslog.send.targets");
			log.info("Sender initialise." +
					" (target=" + targetValue +
					")");
			UdpSender.init(targetValue);
			
			// Receiver設定
			boolean tcpEnable = WinSyslogConfig.getBooleanProperty("syslog.receive.tcp");
			boolean udpEnable = WinSyslogConfig.getBooleanProperty("syslog.receive.udp");
			int port = WinSyslogConfig.getIntegerProperty("syslog.receive.port");
			log.info("Receiver starting." +
					" (tcpEnable=" + tcpEnable +
					", udpEnable=" + udpEnable + 
					", port=" + port + 
					")");
			
			receiver = new SyslogReceiver(tcpEnable, udpEnable, port);
			receiver.start();
			
			Runtime.getRuntime().addShutdownHook(
					new Thread() {
						@Override
						public void run() {
							synchronized (shutdownLock) {
								receiver.shutdown();

								shutdown = true;
								shutdownLock.notify();
							}
						}
					});
			
			synchronized (shutdownLock) {
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

			System.exit(0);
		} catch (Exception e) { 
			log.error("unknown error." , e);
		}
	}

}
