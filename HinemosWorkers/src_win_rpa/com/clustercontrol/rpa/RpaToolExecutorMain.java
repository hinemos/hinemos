/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RPAツールエグゼキューターのメインクラス
 */
public class RpaToolExecutorMain {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RpaToolExecutorMain.class);
	/** shutdownHookが呼ばれるまでmainスレッドを待機させるためのLockオブジェクトおよびフラグ */
	private static final Object shutdownLock = new Object();
	/** 処理終了に使用するフラグ */
	private static boolean shutdown = false;
	/** シナリオ実行指示ファイルの生成を監視するスレッド */
	private static ObserverExecutor runFileObserver;
	/** シナリオ実行中断指示ファイルの生成を監視するスレッド */
	private static ObserverExecutor abortFileObserver;
	/** ログアウト実行指示ファイルの生成を監視するスレッド */
	private static ObserverExecutor logoutFileObserver;
	/** スクリーンショット取得指示ファイルの生成を監視するスレッド */
	private static ObserverExecutor screenshotFileObserver;
	/** PIDファイル名 */
	private static final String PID_FILE_NAME = "_pid_rpa_scenario_executor";
	/** PIDファイル */
	private static File pidFile;
	/** アイコン*/
	private static final String IMAGE_NAME = "icon.gif";

	/**
	 * メイン処理を開始します。
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 引数チェック
			if (args.length != 1) {
				m_log.error("main() : invalid args");
				System.exit(1);
			}
			
			m_log.info("main() : start");

			// pidファイルを生成
			String dataDir = System.getProperty("hinemos.agent.rpa.dir");
			m_log.debug("main() : dataDir=" + dataDir);
			pidFile = new File(dataDir, PID_FILE_NAME);
			updatePidFile();
			
			// タスクトレイアイコンの表示
			showTasktray();

			// プロパティファイル読み込み初期化
			String propFileName = args[0];
			RpaToolExecutorProperties.init(propFileName);

			// 指示ファイル生成監視スレッドの開始
			runFileObserver = new ObserverExecutor(new RunFileObserveTask());
			abortFileObserver = new ObserverExecutor(new AbortFileObserveTask());
			screenshotFileObserver = new ObserverExecutor(new ScreenshotFileObserveTask());
			logoutFileObserver = new ObserverExecutor(new LogoutFileObserveTask());
			runFileObserver.start();
			abortFileObserver.start();
			screenshotFileObserver.start();
			logoutFileObserver.start();

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					m_log.info("main() : shutdownHook called");
					synchronized (shutdownLock) {
						runFileObserver.shutdown();
						abortFileObserver.shutdown();
						screenshotFileObserver.shutdown();
						logoutFileObserver.shutdown();
						shutdown = true;
						shutdownLock.notify();
					}
				}
			});

			synchronized (shutdownLock) {
				while (!shutdown) {
					try {
						m_log.debug("main() : wait until shutdownHook called");
						shutdownLock.wait();
					} catch (InterruptedException e) {
						m_log.warn("shutdown lock interrupted.", e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException sleepE) {
						}
						;
					}
				}
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * pidファイルを生成します。<br>
	 * 既にファイルが存在する場合は終了します。 
	 */
	private static void updatePidFile() {
		checkPidFile();

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String vm = bean.getName();
		m_log.debug("updatePidFile() : vm=" + vm);
		if (vm == null || vm.length() == 0) {
			return;
		}

		String pid = vm.split("@")[0];
		m_log.debug("updatePidFile() : pid=" + pid);

		FileWriter fw = null;
		try {
			fw = new FileWriter(pidFile, false);
			fw.write(pid);
			fw.close();
			fw = null;

		} catch (IOException e) {
			m_log.error("updatePidFile() : pid file creation failed", e);

		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
				fw = null;
			}
		}
	}

	/**
	 * pidファイルが存在するか確認し、存在する場合は終了します。
	 */
	private static void checkPidFile() {
		if (pidFile != null && pidFile.exists()) {
			m_log.error("checkPidFile() : pid file already exists, " + pidFile);
			System.exit(1);
		}
	}
	
	/**
	 * タスクトレイアイコンの表示
	 */
	private static void showTasktray() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			Image image = ImageIO.read(cl.getResourceAsStream(IMAGE_NAME));
			final TrayIcon icon = new TrayIcon(image);
			icon.setImageAutoSize(true);
			icon.setToolTip("Hinemos RPA Scenario Executor");
			SystemTray.getSystemTray().add(icon);
		} catch (IOException | AWTException e) {
			m_log.error("showTasktray() : tasktray creation failed, " + e.getMessage(), e);
		}
		
	}

}
