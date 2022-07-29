/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.tasktray.ha;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.clustercontrol.tasktray.LoginFileObserver;

public class HinemosTasktrayMain {
	// shutdownHookが呼ばれるまでmainスレッドを待機させるためのLockオブジェクトおよびフラグ
	private static final Object shutdownLock = new Object();
	private static boolean shutdown = false;
	private static final String PID_FILE_NAME = "_pid_tasktray_ha";

	private static ServiceObserver observer;
	private static LoginFileObserver loginObserver;
	
	private static File pidFile;

	public static void main(String[] args) {
		try {
			// pidファイルを生成
			String dataDir = System.getProperty("datadir");
			pidFile = new File(dataDir, PID_FILE_NAME);
			updatePidFile();

			observer = new ServiceObserver();
			observer.start();
			loginObserver = new LoginFileObserver();
			loginObserver.start();
			
			Runtime.getRuntime().addShutdownHook(
					new Thread() {
						@Override
						public void run() {
							synchronized (shutdownLock) {
								observer.shutdown();
								loginObserver.shutdown();

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
//						log.warn("shutdown lock interrupted.", e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException sleepE) { };
					}
				}
			}

			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
//			log.error("unknown error." , e);
		}
	}
	private static void updatePidFile() {
		deletePidFile();
		
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String vm = bean.getName();
		if (vm == null || vm.length() == 0) {
			return;
		}
		
		String pid = vm.split("@")[0];
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(pidFile, false);
			fw.write(pid);
			fw.close();
			fw = null;

		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {}
				fw = null;
			}
		}
	}
	
	private static void deletePidFile() {
		if (pidFile != null && pidFile.exists()) {
			boolean flag = pidFile.delete();
			if (!flag) {
				System.out.println("delete failed "+ pidFile.toString());
			}
		}
	}

}
