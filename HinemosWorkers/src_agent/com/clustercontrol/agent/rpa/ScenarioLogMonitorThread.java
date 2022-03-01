/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;

/**
 * RPAシナリオの実行ログを監視するスレッドクラス
 */
public class ScenarioLogMonitorThread extends Thread {
	/** ロガー */
	static private Log m_log = LogFactory.getLog(ScenarioLogMonitorThread.class);

	/**
	 * RPAシナリオの実行ログ監視用ReadingStatusRoot
	 */
	private static AbstractReadingStatusRoot<MonitorInfoWrapper> rpaLogStatusRoot;
	/**
	 * RPAシナリオの実行ログ監視用ReadingStatusDir
	 */
	AbstractReadingStatusDir<MonitorInfoWrapper> rpaLogStatusDir;
	/** RPAシナリオの実行ログ監視用readingstatusディレクトリ */
	private static String readingStatusDir = "/var/rpa/readingstatus";
	/** ログファイル監視設定 */
	private MonitorInfoWrapper monitorInfoWrapper;
	/**
	 * ログファイル監視実行オブジェクト<br>
	 * ファイル名が正規表現で指定されている場合は複数になります。
	 */
	private List<AbstractFileMonitor<MonitorInfoWrapper>> logfileMonitors = new ArrayList<>();
	/**
	 * ログファイル監視を停止するためのフラグ<br>
	 * シナリオ監視スレッドから変更するためvolatileを指定しています。
	 */
	private volatile boolean waiting = true;
	/** スレッド名 */
	private String threadName = "ScenarioLogMonitorThread";
	/** 監視処理が開始したことを表すフラグ */
	private boolean logMonitorStarted = false;

	static {
		/*
		 * RPAシナリオジョブによるログファイル監視のためのReadingStatusRootを生成する
		 * readingstatusディレクトリが存在しない場合は新たに作成し、不要なファイルが存在する場合は削除する
		 * MonitorInfoWrapperは監視実行時に設定するため、ここでは空のListを渡す
		 */
		rpaLogStatusRoot = RpaJobLogfileMonitorManager.getInstance().createReadingStatusRoot(
				new ArrayList<MonitorInfoWrapper>(), getRootStoreDirectory(), RpaJobLogfileMonitorConfig.getInstance());

	}

	/**
	 * コンストラクタ
	 * 
	 * @param monitorInfoWrapper
	 */
	public ScenarioLogMonitorThread(MonitorInfoWrapper monitorInfoWrapper) {
		this.monitorInfoWrapper = monitorInfoWrapper;
		this.setName(threadName);
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				m_log.error("uncaughtException() : " + e.getMessage(), e);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		m_log.info("run() start");
		// ログファイル監視のために必要なReadingStatusDirを作成
		rpaLogStatusDir = rpaLogStatusRoot.createReadingStatusDir(monitorInfoWrapper, getRootStoreDirectory(),
				RpaJobLogfileMonitorConfig.getInstance());
		// 開始時にreadingstatusファイルが存在していた場合は削除
		// ※前回の結果等は引き継がない
		rpaLogStatusDir.clear();
		// terminateが実行されるまで監視を実行
		while (waiting) {
			try {
				// ログファイル情報を更新
				update();
				// ログファイル監視を実行
				for (AbstractFileMonitor<MonitorInfoWrapper> logfileMonitor : logfileMonitors) {
					logfileMonitor.run();
				}
				if (!this.logMonitorStarted) {
					logMonitorStarted = true;
				}
				m_log.debug("run() : sleep " + RpaJobLogfileMonitorConfig.getInstance().getRunInterval() + "ms");
				Thread.sleep(RpaJobLogfileMonitorConfig.getInstance().getRunInterval());
			} catch (InterruptedException e) {
				m_log.info("run() : thread interrupted");
			}
		}
		// ログファイル監視を終了
		for (AbstractFileMonitor<MonitorInfoWrapper> logfileMonitor : logfileMonitors) {
			logfileMonitor.clean();
		}
		// readingstatusファイルを削除
		rpaLogStatusDir.clear();
		m_log.info("run() end");
	}

	/**
	 * 新たに監視対象となるファイルが生成していないか確認し、ログファイル情報を更新します。
	 */
	private void update() {
		// 監視中のログファイル数をリセット
		rpaLogStatusRoot.update();
		// 新たに監視対象となるファイルが生成していないか確認
		rpaLogStatusDir.update();
		// ログファイル監視情報を更新
		logfileMonitors.clear();
		for (AbstractReadingStatus<MonitorInfoWrapper> status : rpaLogStatusDir.list()) {
			logfileMonitors.add(RpaJobLogfileMonitorManager.getInstance().createFileMonitor(monitorInfoWrapper, status,
					RpaJobLogfileMonitorConfig.getInstance()));
			m_log.debug("update() : monitorId=" + monitorInfoWrapper.monitorInfo.getMonitorId() + ", directory="
					+ monitorInfoWrapper.monitorInfo.getLogfileCheckInfo().getDirectory() + ", filename="
					+ monitorInfoWrapper.monitorInfo.getLogfileCheckInfo().getFileName() + ", encoding="
					+ monitorInfoWrapper.monitorInfo.getLogfileCheckInfo().getFileEncoding() + ", lineSeparator="
					+ monitorInfoWrapper.monitorInfo.getLogfileCheckInfo().getFileReturnCode() + ", rsFilepath="
					+ status.getFilePath());
		}
	}

	/**
	 * シナリオログ監視スレッドを停止します。
	 */
	public void terminate() {
		m_log.info("terminate()");
		waiting = false;
		interrupt(); // 待機を即時に終了する
	}

	/**
	 * RPAシナリオの実行ログ監視用readingstatusディレクトリのパスを返します。
	 * 
	 * @return readingstatusディレクトリのパス
	 */
	private static String getRootStoreDirectory() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), readingStatusDir).getAbsolutePath();
		return storepath;
	}

	/**
	 * 監視処理が開始したことを表すフラグを返します。
	 * @return 監視処理が開始したことを表すフラグ
	 */
	public boolean isLogMonitorStarted() {
		return logMonitorStarted;
	}
}
