/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtMonitorInfoResponse;

import org.openapitools.client.model.AgtMonitorPluginStringInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorManager;
import com.clustercontrol.agent.util.RestCalendarUtil;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.bean.CloudConstant;

/**
 * クラウドログ監視の実行を管理するクラス
 */
public class CloudLogMonitor implements Runnable {
	protected ExecutorService _executorService;
	protected ScheduledExecutorService _scheduler;
	protected volatile long lastFireTime;
	protected boolean isInitialRun = true;
	private CloudLogMonitorConfig settingConf;
	protected final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	protected CloudLogMonitorProperty props;
	private AbstractCloudLogMonitorRun runMonitor;
	private boolean hasNotifiedEx = false;
	private boolean hasNotifiedTE = false;
	private AtomicBoolean shutdownFlg = new AtomicBoolean(false);

	private static Log log = LogFactory.getLog(CloudLogMonitor.class);

	/**
	 * スケジューラを初期化しクラウドログ監視固有の設定を読み込み
	 * 
	 * @param info
	 * @param runInfo
	 */
	public CloudLogMonitor(AgtMonitorInfoResponse info, AgtRunInstructionInfoResponse runInfo) {
		props = CloudLogMonitorProperty.getInstance();
		
		// MonitorInfoをCloudLogMonitorConfigに変換
		// クラウドログ監視特有の設定を保存
		List<AgtMonitorPluginStringInfoResponse> list = info.getPluginCheckInfo().getMonitorPluginStringInfoList();
		settingConf = CloudLogMonitorUtil.buildCloudLogSetting(list);
		settingConf.setMonInfo(info);
		settingConf.setRunInfo(runInfo);
		if (runInfo != null) {
			// 監視ジョブ
			settingConf.setMonitorId(runInfo.getSessionId() + runInfo.getJobunitId() + runInfo.getJobId()
					+ runInfo.getFacilityId() + info.getMonitorId());
		} else {
			settingConf.setMonitorId(info.getMonitorId());
		}
		settingConf.setInterval(info.getRunInterval());

		// 一時ファイルの格納場所を指定
		String storepath = CloudLogMonitorUtil.getFileStorePath(settingConf.getMonitorId());
		settingConf.setFilePath(storepath);

	}

	/**
	 * 設定変更時に呼ばれ、設定の変更を反映します
	 * 
	 * @param config
	 */
	public void setConfig(CloudLogMonitorConfig config) {
		this.settingConf = config;
		if (runMonitor == null) {
			log.info("setConfig(): before initial run");
			return;
		}
		runMonitor.setConfig(config);
	}

	/**
	 * 現在の設定を返却します
	 * 
	 * @return
	 */
	public CloudLogMonitorConfig getConfig() {
		return settingConf;
	}

	/**
	 * クラウドログ監視の実行を開始します。
	 */
	public void start() {
		// determine startup delay (using monitorId for random seed)
		int intervalInMillis = settingConf.getInterval() * 1000;
		int delay = new Random(settingConf.getMonitorId().hashCode()).nextInt(60000);

		lastFireTime = settingConf.getLastFireTime();
		shutdownFlg.set(false);

		// initialize scheduler thread
		_scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "CloudLogMonitorScheduler-" + settingConf.getMonitorId());
			}
		});
		// ワーカースレッドの初期化
		_executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "CloudLogMonitorWorker-" + settingConf.getMonitorId());
			}
		});

		// プロパティをログ出力
		log.debug("start(): commonProperties" + props.commonProps());
		log.debug("start(): awsProperties" + props.awsProps());
		log.debug("start(): azureProperties" + props.azureProps());
		
		// 一時ファイル用のディレクトリを作成
		try {
			CloudLogMonitorUtil.createTmpFileDir(this.settingConf.getFilePath());
			CloudLogMonitorUtil.createPropFileDir(CloudLogMonitorUtil.getPropFileStorePath(this.settingConf.getMonitorId()));
		} catch (HinemosUnknown e) {
			// ディレクトリの作成に失敗した場合は、監視が続行できないので通知して終了
			CloudLogMonitorUtil.sendMessage(getConfig(), PriorityConstant.TYPE_WARNING,
					MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_TMP_FILE.getMessage(), e.getMessage());
			return;
		}

		// start
		isInitialRun = true;
		log.debug("start(): CloudLogMonitor MonitorID: " + settingConf.getMonitorId() + " submitted");

		// プラットフォーム毎に処理を分岐
		if (settingConf.getPlatform().equals(CloudConstant.platform_AWS)) {
			runMonitor = new CloudLogMonitorRunAWS(this.settingConf);
		} else {
			runMonitor = new CloudLogMonitorRunAzure(this.settingConf);

		}
		_scheduler.scheduleWithFixedDelay(this, delay, intervalInMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * スケジューラから定期的に呼ばれるメソッド 初回実行時の動作のみコントロールし、実際の取得は
	 * CloudLogMonitorRunAWSおよびCloudLogMonitorRunAzureで行う
	 */

	@Override
	public void run() {
		log.debug("run started");

		try {
			// 初回監視実行タイミングでは何もしない
			if (lastFireTime == 0) {
				log.info("run(): initial run for Monitor ID: " + settingConf.getMonitorId());
				lastFireTime = CloudLogMonitorUtil.getTimeWithOffset();
				isInitialRun = false;
				// 初回実行タイミングでファイル監視を初期化
				runMonitor.execFileMonitor();

				return;
			}

			// 再開時は遡って取得するかを確認
			if (isInitialRun) {
				long missingDuration = CloudLogMonitorUtil.getTimeWithOffset() - settingConf.lastFireTime;
				isInitialRun = false;

				boolean shouldDeleteFilesAndReturn = false;
				if (!props.isMissingEnable()) {
					log.info("run(): Get missing log disabled");
					shouldDeleteFilesAndReturn = true;
				} else if (missingDuration > props.getMissingPeriod()) {
					log.info("run(): Missing period exceeds threshold. Missing period: " + missingDuration
							+ " Threashold: " + props.getMissingPeriod());
					shouldDeleteFilesAndReturn = true;
				}
				// マネージャから渡された最終取得日時から再開しない場合は、
				// リーディングステータスのキャリーオーバが不要になるので、
				// 一時ファイルを削除して、ファイル監視を初期化する。
				if (shouldDeleteFilesAndReturn) {
					lastFireTime = CloudLogMonitorUtil.getTimeWithOffset();
					File directory = new File(settingConf.getFilePath());
					CloudLogMonitorUtil.deleteOnlyTmpFiles(directory);
					// 初回実行タイミングでファイル監視を初期化
					runMonitor.execFileMonitor();
					return;
				}
				
				// マネージャから渡された最終取得日時から再開
				log.info("run(): resume from " + settingConf.lastFireTime);
				// 前回の監視時にクラウドログ監視の処理が正常に完了せず、
				// 一時ファイルにログの書き出しまでは行われているが、監視されなかったような場合に
				// 重複してログを監視してしまう可能性がある。（今回のログ取得でも同じログを取得するため）
				// 重複を防ぐため、再開時は必ず一時ファイルを0バイトにしておく。
				// ※キャリーオーバはrsファイルに保存されるので影響は受けない
				File directory = new File(settingConf.getFilePath());
				CloudLogMonitorUtil.truncateTmpFilesRecursive(directory,this.settingConf.getMonitorId());
				// 初回実行タイミングでファイル監視を初期化
				runMonitor.execFileMonitor();
			} else {
				log.info("run(): start from " + lastFireTime);
			}
			isInitialRun = false;

			// カレンダを確認し、ログの取得を行うかを判定
			// offsetをいじってfrom>=toになった場合もログ監視は行わない
			if (!shouldRunNow(lastFireTime, CloudLogMonitorUtil.getTimeWithOffset())) {
				// 取得間隔がすべて非稼働時間帯にかかっていた場合は、ログの取得をせずに終了
				log.debug("run(): calendar set to not operate or from is bigger than to. From: " + lastFireTime + " To: "
						+ CloudLogMonitorUtil.getTimeWithOffset());
				lastFireTime = CloudLogMonitorUtil.getTimeWithOffset();
				CloudLogMonitorUtil.sendLastFireTime(this.settingConf, lastFireTime);
				return;
			}

			runMonitor.setLastFireTime(lastFireTime);
			Future<?> result = _executorService.submit(runMonitor);
			boolean isSuccess = true;
			int waitTime = 0;
			while (true) {
				try {
					result.get(getConfig().getInterval(), TimeUnit.SECONDS);
					break;
				} catch (InterruptedException | ExecutionException e) {
					
					if (!shutdownFlg.get()) {
						log.error("run(): Unknown Error.", e);
						// マネージャに通知
						if (!hasNotifiedEx) {
							CloudLogMonitorUtil.sendMessage(getConfig(), PriorityConstant.TYPE_WARNING,
									MessageConstant.AGENT.getMessage(),
									MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_UNKNOWN.getMessage(),
									e.getMessage());
							hasNotifiedEx = true;
						}
					} else {
						// 監視終了時にはExceptionが発生する可能性があるが、動作に影響はないので通知しない
						log.debug("run(): Exception during shutdown.", e);
					}
					
					isSuccess = false;
					break;
				} catch (TimeoutException e) {
					waitTime += getConfig().getInterval();
					String[] args = { this.settingConf.getMonitorId(), "" + waitTime, "" + getConfig().getInterval() };
					// マネージャに通知
					if (!hasNotifiedTE) {
						CloudLogMonitorUtil.sendMessage(getConfig(), PriorityConstant.TYPE_WARNING,
								MessageConstant.AGENT.getMessage(),
								MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_TAKING_LONG_TIME.getMessage(args),
								e.getMessage());
						hasNotifiedTE = true;
					}
					log.warn("run(): Timeout occured. Run Duration: " + waitTime);
					isSuccess = false;
				}
			}
			isSuccess = runMonitor.isSucceed();

			// 正常終了した場合は最終実行日時を取得
			if (isSuccess) {
				// INTERNALからの回復の場合は通知
				CloudLogMonitorUtil.notifyRecovery(getConfig(), hasNotifiedEx | hasNotifiedTE);
				lastFireTime = runMonitor.getLastFireTime();
				hasNotifiedEx = false;
				hasNotifiedTE = false;
			} else {
				lastFireTime = CloudLogMonitorUtil.getTimeWithOffset();
			}

			CloudLogMonitorUtil.sendLastFireTime(this.settingConf, lastFireTime);
			// 更新の無い不要なファイルを削除
			removeTmpFile(true);
			log.debug("run ended: Monitor ID" + settingConf.getMonitorId() + " " + "lastFireTime=" + lastFireTime);
		} catch (Throwable t) {
			log.error("run(): Unexcepted error occured.", t);
		}
	}
	
	/**
	 * カレンダが設定されている場合に、今回の取得間隔でログを取得すべきかを確認します。
	 * 取得間隔がすべて非稼働時間の場合は、ログの取得を行いません。
	 * @param from
	 * @param to
	 * @return
	 */
	private boolean shouldRunNow(long from, long to){
		
		// 取得間隔を一秒ごとに確認する
		while (from <= to){
			Date tmpDate = new Date(from);
			if (RestCalendarUtil.isRun(getConfig().getMonInfo().getCalendar(), tmpDate)){
				return true;
			}
			from += 1000;
		}
		
		return false;
	}

	/**
	 * 設定変更時に呼ばれます スケジューラの再スケジュールが必要かを判断し、 必要に応じて再スケジュールします。
	 * 
	 * @param newMonitor
	 */
	public void update(CloudLogMonitor newMonitor) {
		// プラットフォームやインターバルが変更になった場合は再スケジュール
		if (settingConf.compare(newMonitor.getConfig())) {
			setConfig(newMonitor.getConfig());
		} else {
			log.info("update(): Setting changed. Initialize. Monitor ID: " + newMonitor.getConfig().getMonitorId());
			shutdown();
			setConfig(newMonitor.getConfig());
			start();
		}
	}

	/**
	 * 一時ファイルを削除しスケジューラをシャットダウンします。
	 */
	public void shutdown() {
		log.info("shutdown CloudLog monitor. MonitorID: " + settingConf.getMonitorId());
		shutdownFlg.set(true);
		// 一時ファイルへの参照をクローズ
		CloudLogfileMonitorManager.getInstance().cleanCloudLogMonitorFiles(settingConf.getMonitorId());
		// 一時ファイルの削除
		removeTmpFile(false);
		// readingstatusの削除
		CloudLogfileMonitorManager.getInstance().clearReadingStatus();
		// スケジューラのシャットダウン
		shutdownWorkers();
	}
	
	/**
	 * スケジューラのみをシャットダウンします。
	 * エージェント停止時に使用することを想定しています。
	 */
	public boolean shutdownWorkers() {
		boolean hasTimeout = false;
		_scheduler.shutdown();
		_executorService.shutdown();
		long shutdownTimeoutMsec = props.getAwaitTerminationPeriod();
		try {
			if (!_scheduler.awaitTermination(shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _scheduler.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
					hasTimeout = true;
				}
			}
			if (!_executorService.awaitTermination(shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _executorService.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
					hasTimeout = true;
				}
			}
		} catch (InterruptedException e) {
			_executorService.shutdownNow();
		}
		return hasTimeout;
	}

	/**
	 * 不要になった一時ファイルを削除します。
	 */
	private void removeTmpFile(boolean partial) {
		File directory = new File(settingConf.getFilePath());
		File propDirectory = new File(CloudLogMonitorUtil.getPropFileStorePath(settingConf.getMonitorId()));
		List<String> deletedList = null;
		try {
			if (!partial) {
				CloudLogMonitorUtil.deleteDirectoryRecursive(directory);
				// 監視終了時はプロパティファイルも全削除する
				CloudLogMonitorUtil.deleteDirectoryRecursive(propDirectory);
			} else {
				deletedList = CloudLogMonitorUtil.deleteOldFiles(directory);
				// 一時ファイルを削除した場合、対応するプロパティファイルも削除する
				if (deletedList != null) {
					for (String fileName : deletedList) {
						log.debug("removeTmpFile(): deleted file name : " + fileName);
						// ローテーションしたファイルは無視
						if (fileName.matches(".*\\.tmp$")) {
							for (String key : runMonitor.statusMap.keySet()) {
								log.debug("removeTmpFile(): prop key : " + key);
								String fileKey = "";
								// プロパティファイル名を取得
								fileKey = runMonitor.getPropFileName(key);
								if (fileName.contains(fileKey)) {
									CloudLogMonitorStatus removed = runMonitor.statusMap.remove(key);
									// nullにはならないはずだが念のため。
									if (removed != null) {
										removed.clear();
										log.info("removeTmpFile(): prop file removed. Prop target: "
												+ removed.getMonitorTarget());
										break;
									}
								}
							}
						}
					}

				}
				// エージェント起動後一度も該当のログストリームからログを取得していない場合、
				// CloudLogMonitorStatusは作成されていないがプロパティファイルのみ存在する場合があるので、
				// 更新のないものは削除
				CloudLogMonitorUtil.deleteOldFiles(propDirectory);
			}
		} catch (Exception e) {
			log.warn("removeTmpFile(): failed remove file", e);
		}

	}

}