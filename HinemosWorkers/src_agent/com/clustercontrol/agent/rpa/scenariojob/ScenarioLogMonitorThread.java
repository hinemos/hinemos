/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;

/**
 * RPAシナリオの実行ログを監視するスレッドクラス
 */
public class ScenarioLogMonitorThread extends Thread {
	/** ロガー */
	static private Log m_log = LogFactory.getLog(ScenarioLogMonitorThread.class);

	/**
	 * RPAシナリオの実行ログ監視用ReadingStatusRoot
	 */
	private static ScenarioLogReadingStatusRoot rpaLogStatusRoot;
	/**
	 * RPAシナリオの実行ログ監視用ReadingStatusDir
	 */
	ScenarioLogReadingStatusDir rpaLogStatusDir;
	/** RPAシナリオの実行ログ監視用readingstatusディレクトリ */
	private static String readingStatusDir = "/var/rpa/readingstatus";
	/** ログファイル監視設定 */
	private MonitorInfoWrapper monitorInfoWrapper;
	/**
	 * ログファイル監視実行オブジェクト<br>
	 * ファイル名が正規表現で指定されている場合は複数になります。
	 */
	private List<ScenarioLogfileMonitor> logfileMonitors = new ArrayList<>();
	/**
	 * ログファイル監視を停止するためのフラグ<br>
	 * シナリオ監視スレッドから変更するためvolatileを指定しています。
	 */
	private volatile boolean waiting = true;
	/** スレッド名 */
	private String threadName = "ScenarioLogMonitorThread";
	/** 監視処理が開始したことを表すフラグ */
	private boolean logMonitorStarted = false;
	/**
	 * ログファイル監視によるエラーメッセージ<br>
	 * シナリオ監視スレッドから参照するためConcurrentHashを想定。
	 */
	private Map<String,StringBuilder> errorMessageForLogfileMonitor = new ConcurrentHashMap<String,StringBuilder>();
	/**
	 * ファイル最大数超過による無視リスト<br>
	 */
	private Map<String,String> ignoreListForFileCounter = new ConcurrentHashMap<String,String>();

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

		ignoreListForFileCounter.clear();
		errorMessageForLogfileMonitor.clear();

		// ログファイル監視のために必要なReadingStatusDirを作成
		// ※前回の結果等は引き継がず、ジョブ起動前のログは読み飛ばす（そのため一度clearし、ScenarioLogReadingStatusDir#createReadingStatusで tailをtrueに固定 ）
		rpaLogStatusDir = rpaLogStatusRoot.createReadingStatusDir(monitorInfoWrapper, getRootStoreDirectory(),RpaJobLogfileMonitorConfig.getInstance());
		if( m_log.isDebugEnabled()){
			for (AbstractReadingStatus<MonitorInfoWrapper> rs : rpaLogStatusDir.list()) {
				m_log.debug("run() : rpaLogStatusDir : path=" +rs.getFilePath()+ ",position=" + rs.getPosition() +",prevsize="+rs.getPrevSize());
			}
		}
		rpaLogStatusDir.clear();

		// terminateが実行されるまで監視を実行
		while (waiting) {
			try {
				// ログファイル情報を更新
				update();
				// ログファイル監視を実行
				for (ScenarioLogfileMonitor logfileMonitor : logfileMonitors) {
					logfileMonitor.run();
					// ファイル監視で異常が有れば呼び出し元へ連携出来るように設定
					if(logfileMonitor.getErrorMessageBuilder() != null && logfileMonitor.getErrorMessageBuilder().length()>0){
						errorMessageForLogfileMonitor.put(logfileMonitor.getMonitorFilePath(), logfileMonitor.getErrorMessageBuilder());
					}
				}
				if (!this.logMonitorStarted) {
					logMonitorStarted = true;
				}
				m_log.debug("run() : sleep " + RpaJobLogfileMonitorConfig.getInstance().getRunInterval() + "ms");
				Thread.sleep(RpaJobLogfileMonitorConfig.getInstance().getRunInterval());
			} catch (InterruptedException e) {
				m_log.info("run() : thread interrupted");
			} finally {
				// ログファイル監視を終了
				for (AbstractFileMonitor<MonitorInfoWrapper> logfileMonitor : logfileMonitors) {
					logfileMonitor.clean();
				}
			}
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
		// 対象ファイルの件数が上限超フラグを呼び出し元へ連携出来るように設定
		if (rpaLogStatusDir.getIgnoreListForFileCounter() != null
				&& rpaLogStatusDir.getIgnoreListForFileCounter().size() > 0) {
			for(Map.Entry<String,String> rec : rpaLogStatusDir.getIgnoreListForFileCounter().entrySet()){
				this.ignoreListForFileCounter.put(rec.getKey(), rec.getValue());
			}
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

	public Map<String,StringBuilder> getErrorMessageForLogfileMonitor() {
		return errorMessageForLogfileMonitor;
	}

	public Map<String,String> getIgnoreListForFileCounter() {
		return this.ignoreListForFileCounter;
	}
}
