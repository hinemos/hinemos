/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtLogfileCheckInfoResponse;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtMonitorStringValueInfoResponse;
import org.openapitools.client.model.AgtRpaJobEndValueConditionInfoRequest;
import org.openapitools.client.model.AgtRpaJobEndValueConditionInfoResponse;
import org.openapitools.client.model.AgtRpaJobRoboRunInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.SetJobResultRequest;
import org.openapitools.client.model.SetJobResultRequest.RpaJobErrorTypeEnum;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.job.AgentThread;
import com.clustercontrol.agent.job.RunHistoryUtil;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RoboAbortInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboLogoutInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboResultInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobEndValueConditionTypeConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant;
import com.clustercontrol.jobmanagement.rpa.util.ReturnCodeConditionChecker;
import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;
import com.clustercontrol.jobmanagement.util.JobCommonUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * RPAシナリオを実行し監視するスレッドクラス
 */
public class ScenarioMonitorThread extends AgentThread {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(ScenarioMonitorThread.class);
	
	/** インスタンスを格納 */
	private static Map<String, ScenarioMonitorThread> instances = new ConcurrentHashMap<>();
	
	/** ジョブ実行結果送信用オブジェクト */
	private JobResultSendableObject jobResult = new JobResultSendableObject();

	/** RPAツールエグゼキューター連携用ファイル管理オブジェクト */
	private RoboFileManager roboFileManager;

	/** 実行結果ファイルチェック間隔 */
	public static int checkInterval = 10000; // 10sec

	/** 実行指示ファイルチェック間隔 */
	public static int instConfirmInterval = 5000; // 5sec

	/** 実行指示ファイルチェックタイムアウト */
	public static int instConfirmTimeout = 20000; // 20sec

	/** ログアウト完了チェック間隔 */
	public static int logoutConfirmInterval = 5000; // 5sec

	/** ログアウト完了チェックタイムアウト */
	public static int logoutConfirmTimeout = 60000; // 60sec

	/** エグゼキューターへのログアウト指示を始めたか否か **/
	private boolean isLogoutStarted = false;
	
	/** セッションがアクティブ（デスクトップログイン中）なユーザー名 **/
	private String activeUserName = null;

	private static final String key1 = "job.rpa.result.check.interval";
	private static final String key2 = "job.rpa.instruction.confirm.interval";
	private static final String key3 = "job.rpa.instruction.confirm.timeout";
	private static final String key4 = "job.rpa.logout.confirm.interval";
	private static final String key5 = "job.rpa.logout.confirm.timeout";

	static {
		try {
			String checkIntervalStr = AgentProperties.getProperty(key1, Integer.toString(checkInterval));
			checkInterval = Integer.parseInt(checkIntervalStr);
		} catch (Exception e) {
			m_log.warn("ScenarioMonitorThread : " + e.getMessage(), e);
		}
		m_log.info(key1 + "=" + checkInterval);
		
		try {
			String instructionConfirmIntervalStr = AgentProperties.getProperty(key2, Integer.toString(instConfirmInterval));
			instConfirmInterval = Integer.parseInt(instructionConfirmIntervalStr);
		} catch (Exception e) {
			m_log.warn("ScenarioMonitorThread : " + e.getMessage(), e);
		}
		m_log.info(key2 + "=" + instConfirmInterval);
		
		try {
			String instructionConfirmTimeoutStr = AgentProperties.getProperty(key3, Integer.toString(instConfirmTimeout));
			instConfirmTimeout = Integer.parseInt(instructionConfirmTimeoutStr);
		} catch (Exception e) {
			m_log.warn("ScenarioMonitorThread : " + e.getMessage(), e);
		}
		m_log.info(key3 + "=" + instConfirmTimeout);
		
		try {
			String logoutConfirmIntervalStr = AgentProperties.getProperty(key4, Integer.toString(logoutConfirmInterval));
			logoutConfirmInterval = Integer.parseInt(logoutConfirmIntervalStr);
		} catch (Exception e) {
			m_log.warn("ScenarioMonitorThread : " + e.getMessage(), e);
		}
		m_log.info(key4 + "=" + logoutConfirmInterval);
		
		try {
			String logoutConfirmTimeoutStr = AgentProperties.getProperty(key5, Integer.toString(logoutConfirmTimeout));
			logoutConfirmTimeout = Integer.parseInt(logoutConfirmTimeoutStr);
		} catch (Exception e) {
			m_log.warn("ScenarioMonitorThread : " + e.getMessage(), e);
		}
		m_log.info(key5 + "=" + logoutConfirmTimeout);
	}


	/**
	 * コンストラクタ
	 * 
	 * @param info
	 * @param sendQueue
	 */
	public ScenarioMonitorThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue ,String activeUserName) {
		super(info, sendQueue);
		this.setName(this.getClass().getSimpleName());
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				m_log.error("uncaughtException() : " + e.getMessage(), e);
			}
		});
		this.activeUserName = activeUserName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		m_log.info("run() start");

		// ジョブ停止時に使用できるようインスタンスをマップに格納
		instances.put(RunHistoryUtil.getKey(m_info), this);
		ScenarioLogMonitorThread logMonitorThread = null;

		try {
			// 古いファイルが存在する場合削除しておく
			try {
				roboFileManager = new RoboFileManager();
			} catch (InterruptedException e) {
				m_log.error("run() : interrupted. e=" + e.getMessage(), e);
				return;
			}
			roboFileManager.clear();

			if (isLogConditionExisting()) {
				// ログによる終了値判定条件が存在する場合
				// ログファイル監視設定を生成し、シナリオログ監視スレッドを開始
				logMonitorThread = new ScenarioLogMonitorThread(createMonitorInfoWrapper());
				logMonitorThread.start();
				while (!Thread.currentThread().isInterrupted()) {
					if (logMonitorThread.isLogMonitorStarted()) {
						m_log.debug("read() : ScenarioLogMonitor started.");
						break;
					} else {
						try {
							// ログファイル監視が開始してからシナリオを実行する必要があるため待機
							m_log.debug("read() : waiting for ScenarioLogMonitor started.");
							Thread.sleep(checkInterval);
						} catch (InterruptedException e) {
							m_log.warn("run() : thread interrutped", e);
							break;
						}
					}
				}
			}

			// RPAツールエグゼキューターへの実行指示
			RoboRunInfo roboRunInfo = convertDTO(m_info.getRpaRoboRunInfo());
			// 一般ユーザでの実行可能性があるためEveryoneのフルアクセスで設定
			roboFileManager.writeWithEveryoneFullAccess(roboRunInfo);

			// 指示受付確認待ち
			boolean retRunInst = roboFileManager.confirmDelete(RoboRunInfo.class, true, instConfirmInterval, instConfirmTimeout);
			if (!retRunInst) {
				m_log.warn("run() : The run instruction may not have been communicated to the executor. session="
						+ m_info.getSessionId());
				//停止指示があった場合、指示が受け付けられていない可能性を考慮して、指示ファイルの削除を試行する。
				if( roboFileManager.isAborted() ){
					roboFileManager.deleteIfExist(RoboRunInfo.class);
				}
			}

			// RPAツールエグゼキューターの実行結果取得
			// 実行結果ファイルが生成するまでここで待機する
			// ジョブが停止された場合は待機を中断しここから処理を再開する
			RoboResultInfo roboResultInfo = roboFileManager.read(RoboResultInfo.class, checkInterval);
			m_log.debug("run() : roboResultInfo=" + roboResultInfo);

			Integer returnCode = null;
			Integer status, endValue;
			StringBuilder message = new StringBuilder();
			String errorMessage, logfileName, logMessage;
			errorMessage = logfileName = logMessage = "";
			AgtRpaJobEndValueConditionInfoResponse endValueCondition = null;
			if (roboResultInfo != null) {
				if (roboResultInfo.getError()) {
					// RPAツールのプロセス起動に失敗した場合
					m_log.warn("run() : rpa tool execution failed");
					status = RunStatusConstant.ERROR;
					endValue = -1;
					errorMessage = roboResultInfo.getErrorMessage();
				} else {
					status = RunStatusConstant.END;
					returnCode = roboResultInfo.getReturnCode();
					try {
						// RPAツール終了時のログを検知する必要があるため監視間隔だけ待機
						Thread.sleep(RpaJobLogfileMonitorConfig.getInstance().getRunInterval());
					} catch (InterruptedException e) {
						m_log.warn("run() : thread interrutped", e);
					}
					// ログとリターンコードから終了値を判定
					EndValueChecker endValueChecker = new EndValueChecker(m_info, returnCode);
					endValueChecker.addStrategy(new LogEndValueCheckStrategy(m_info));
					endValueChecker.addStrategy(new ReturnCodeEndValueCheckStrategy(returnCode));
					endValue = endValueChecker.check();
					// 一致した終了値判定条件
					// どの条件にも一致しなかった場合はnull
					endValueCondition = endValueChecker.getSatisfiedCondition();
					if (endValueCondition != null
							&& endValueCondition.getConditionType() == RpaJobEndValueConditionTypeConstant.LOG) {
						// 一致した判定条件がログの場合、ログメッセージとログファイル名を取得
						logMessage = ScenarioLogCache.get(m_info).getLogMessage();
						logfileName = ScenarioLogCache.get(m_info).getLogfileName();
					}
					// スクリーンショットの取得条件をチェック
					if (m_info.getRpaScreenshotEndValue() != null) {
						if (checkScreenshotEndValueCondition(endValue)) {
							m_info.setRpaScreenshotTriggerType(AgtRunInstructionInfoResponse.RpaScreenshotTriggerTypeEnum.END_VALUE);
							if (new ScreenshotThread(m_info, m_sendQueue, this.activeUserName).takeScreenshot()) {
								m_log.debug("run() : success to take screenshot.");
								message.append(MessageConstant.MESSAGE_JOB_RPA_TAKE_SCREENSHOT.getMessage());
							} else {
								m_log.debug("run() : fail to take screenshot.");
								message.append(MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_FAIL.getMessage());
							}
						}
					}
					// 監視ファイル最大数エラーがあれば通知メッセージに追加
					if (logMonitorThread != null && logMonitorThread.isIgnoreListForFileCounter()) {
						if(m_log.isDebugEnabled()){
							m_log.debug("run() : a job message was set that there is too many file. ,session="
									+ m_info.getSessionId());
						}
						message.append(MessageConstant.MESSAGE_JOB_RPA_LOG_FILE_TOO_MANY_FILES.getMessage() + "\n");
					}
					// 	各ファイル監視中の各種エラーがあれば通知メッセージに追加
					if (logMonitorThread != null && logMonitorThread.getErrorMessageForLogfileMonitor() != null
							&& logMonitorThread.getErrorMessageForLogfileMonitor().size() > 0) {
						for( Map.Entry<String,StringBuilder> rec : logMonitorThread.getErrorMessageForLogfileMonitor().entrySet() ){
							if(rec.getValue() != null && rec.getValue().length() > 0){
								message.append(rec.getValue().toString()+"\n");
							}
						}
					}
					// ログインを行った場合、ログアウトするかどうかをチェック
					if (m_info.getRpaRoboRunInfo().getLogin()) {
						if (checkLogout(endValue)) {
							if (!message.toString().isEmpty()) {
								message.append("\n");
							}
							message.append(MessageConstant.MESSAGE_JOB_RPA_EXEC_LOGOUT.getMessage());
						}
					}
				}
			} else {
				// ジョブが停止された場合にこちらを通る
				status = RunStatusConstant.END;
				endValue = -1;
				// RPAツールエグゼキューターへのシナリオ実行中断指示ファイルを生成
				RoboAbortInfo roboAbortInfo = new RoboAbortInfo(roboRunInfo.getDatetime(), roboRunInfo.getSessionId(),
						roboRunInfo.getJobunitId(), roboRunInfo.getJobId(), roboRunInfo.getFacilityId(), this.activeUserName);
				m_log.debug("run() : " + roboAbortInfo);
				// 一般ユーザでの実行可能性があるためEveryoneのフルアクセスで設定
				roboFileManager.writeWithEveryoneFullAccess(roboAbortInfo);
				// 指示受付確認待ち
				boolean retAbortInst = roboFileManager.confirmDelete(RoboAbortInfo.class, false, instConfirmInterval,
						instConfirmTimeout);
				if (!retAbortInst) {
					m_log.warn("run() : The abort instruction may not have been communicated to the executor. session="
							+ m_info.getSessionId());
				}
				//停止した旨をマネージャに送信
				sendStopMessage();
			}

			// 実行結果ファイルを削除
			roboFileManager.deleteIfExist(RoboResultInfo.class);

			// ジョブ実行結果を送信
			sendJobResult(status, endValue, returnCode, endValueCondition, message.toString(), errorMessage,
					logfileName, logMessage);

		} catch (IOException | HinemosException e) {
			// RPAツールエグゼキューター連携用ファイル入出力エラーが発生
			// ジョブ実行結果を送信
			sendJobResult(RunStatusConstant.ERROR, -1, null, null, "", e.getMessage(), "", "");

		} finally {
			// シナリオログ監視スレッドの終了
			if (logMonitorThread != null) {
				logMonitorThread.terminate();
			}

			// 終了値判定用データを削除
			ScenarioLogCache.remove(m_info);

			// 実行履歴から削除
			RunHistoryUtil.delRunHistory(m_info);
			instances.remove(RunHistoryUtil.getKey(m_info));
		}
	}

	/**
	 * マネージャにジョブ実行結果を送信します。<br>
	 * マッチした終了値判定条件をクライアントで表示するために必要な情報も合わせて送信します。
	 * 
	 * @param status
	 *            ジョブの終了ステータス
	 * @param endValue
	 *            終了値
	 * @param returnCode
	 *            RPAツールのリターンコード
	 * @param endValueCondition
	 *            マッチした終了値判定条件
	 * @param message
	 *            メッセージ
	 * @param errorMessage
	 *            エラーメッセージ
	 * @param logfileName
	 *            RPAツールのログファイル名
	 * @param logMessage
	 *            RPAツールのログメッセージ
	 */
	private void sendJobResult(Integer status, Integer endValue, Integer returnCode,
			AgtRpaJobEndValueConditionInfoResponse endValueCondition, String message, String errorMessage,
			String logfileName, String logMessage) {
		m_log.debug("sendJobResult() : status=" + status + ", endValue=" + endValue + ", returnCode=" + returnCode
				+ ", endValueCondition=" + endValueCondition + ", message=" + message + ", errorMessage" + errorMessage
				+ ", logfileName=" + logfileName + ", logMessage=" + logMessage);
		jobResult.sessionId = m_info.getSessionId();
		jobResult.jobunitId = m_info.getJobunitId();
		jobResult.jobId = m_info.getJobId();
		jobResult.facilityId = m_info.getFacilityId();
		jobResult.body = new SetJobResultRequest();
		jobResult.body.setCommand(m_info.getCommand());
		jobResult.body.setCommandType(m_info.getCommandType());
		jobResult.body.setStatus(status);
		jobResult.body.setEndValue(endValue);
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
		jobResult.body.setMessage(message);
		jobResult.body.setErrorMessage(errorMessage);
		jobResult.body.setRpaJobReturnCode(returnCode);
		jobResult.body.setRpaJobLogfileName(logfileName);
		jobResult.body.setRpaJobLogMessage(logMessage);
		if (endValueCondition != null) {
			jobResult.body.setRpaJobEndValueConditionInfo(convertDTO(endValueCondition));
		}
		if (status == RunStatusConstant.ERROR) {
			jobResult.body.setRpaJobErrorType(RpaJobErrorTypeEnum.OTHER);
		}
		m_sendQueue.put(jobResult);
	}

	/**
	 * シナリオログ監視スレッドで使用するログファイル監視設定を生成します。
	 * 
	 * @return ログファイル監視設定
	 */
	private MonitorInfoWrapper createMonitorInfoWrapper() {
		// 監視項目IDはセッションID、ジョブユニットID、ジョブID、終了値判定条件の順序を結合したものにする
		final String MONITOR_ID_PREFIX = "RPA_LOGFILE_";
		String monitorId = MONITOR_ID_PREFIX + m_info.getSessionId() + "_" + m_info.getJobunitId() + "_"
				+ m_info.getJobId();
		AgtMonitorInfoResponse monitorInfo = new AgtMonitorInfoResponse();
		monitorInfo.setMonitorId(monitorId);
		// RPAシナリオジョブでは通常のログファイル監視を使用する
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_LOGFILE);
		monitorInfo.setRegDate(HinemosTime.currentTimeMillis());
		monitorInfo.setMonitorFlg(true);
		monitorInfo.setCollectorFlg(false); // 収集は無効
		AgtLogfileCheckInfoResponse logfileCheckInfo = new AgtLogfileCheckInfoResponse();
		logfileCheckInfo.setDirectory(m_info.getRpaLogDirectory());
		logfileCheckInfo.setFileName(m_info.getRpaLogFileName());
		logfileCheckInfo.setFileEncoding(m_info.getRpaLogFileEncoding());
		logfileCheckInfo.setFileReturnCode(m_info.getRpaLogFileReturnCode());
		logfileCheckInfo.setPatternHead(m_info.getRpaLogPatternHead());
		logfileCheckInfo.setPatternTail(m_info.getRpaLogPatternTail());
		logfileCheckInfo.setMaxBytes(m_info.getRpaLogMaxBytes());
		monitorInfo.setLogfileCheckInfo(logfileCheckInfo);
		m_log.debug("buildMonitorInfoWrapper() : monitorId=" + monitorId + ", directory=" + m_info.getRpaLogDirectory()
				+ ", filename=" + m_info.getRpaLogFileName() + ", fileEncoding=" + m_info.getRpaLogFileEncoding()
				+ ", fileReturnCode=" + m_info.getRpaLogFileReturnCode() + ", filePatternHead="
				+ m_info.getRpaLogPatternHead() + ", filePatternTail=" + m_info.getRpaLogPatternTail()
				+ ", fileMaxBytes=" + m_info.getRpaLogMaxBytes());

		List<AgtMonitorStringValueInfoResponse> stringValueInfos = new ArrayList<>();
		for (AgtRpaJobEndValueConditionInfoResponse endValueCondition : m_info.getRpaEndValueConditionInfoList()) {
			// シナリオ実行ログよる終了値判定条件に対する文字列パターンマッチ設定を作成
			if (endValueCondition.getConditionType() == RpaJobEndValueConditionTypeConstant.LOG) {
				AgtMonitorStringValueInfoResponse stringValueInfo = new AgtMonitorStringValueInfoResponse();
				stringValueInfo.setMonitorId(monitorId);
				stringValueInfo.setOrderNo(endValueCondition.getOrderNo()); // 監視結果と終了値判定条件の対応付けで必要なためorderNoを格納しておく
				stringValueInfo.setPattern(endValueCondition.getPattern());
				stringValueInfo.setCaseSensitivityFlg(endValueCondition.getCaseSensitivityFlg());
				stringValueInfo.setProcessType(endValueCondition.getProcessType());
				stringValueInfo.setValidFlg(true);
				stringValueInfos.add(stringValueInfo);
				m_log.debug("buildMonitorInfoWrapper() : orderNo=" + endValueCondition.getOrderNo() + ", pattern="
						+ endValueCondition.getPattern() + ", caseSensitivityFlg="
						+ endValueCondition.getCaseSensitivityFlg() + ", processType="
						+ endValueCondition.getProcessType());
			}
		}
		monitorInfo.setStringValueInfo(stringValueInfos);
		monitorInfo.setUpdateDate(Long.MAX_VALUE);//null回避用のダミー値
		MonitorInfoWrapper monitorInfoWrapper = new MonitorInfoWrapper(monitorInfo, m_info);
		return monitorInfoWrapper;
	}

	/**
	 * レスポンスDTOをRPAシナリオ実行指示DTOに変換します。
	 * 
	 * @param dto
	 *            レスポンスDTO
	 * @return 変換後のDTO
	 */
	private RoboRunInfo convertDTO(AgtRpaJobRoboRunInfoResponse dto) {
		m_log.debug("convertDTO() : " + dto);
		RoboRunInfo roboRunInfo = new RoboRunInfo();
		roboRunInfo.setDatetime(dto.getDatetime());
		roboRunInfo.setSessionId(dto.getSessionId());
		roboRunInfo.setJobunitId(dto.getJobunitId());
		roboRunInfo.setJobId(dto.getJobId());
		roboRunInfo.setFacilityId(dto.getFacilityId());
		roboRunInfo.setExecCommand(dto.getExecCommand());
		roboRunInfo.setDestroyCommand(dto.getDestroyCommand());
		roboRunInfo.setLogin(dto.getLogin());
		roboRunInfo.setLogout(dto.getLogout());
		roboRunInfo.setDestroy(dto.getDestroy());
		roboRunInfo.setUserName(this.activeUserName);
		return roboRunInfo;

	}

	/**
	 * 終了値判定条件レスポンスDTOをリクエストDTOに変換します。
	 * 
	 * @param dto
	 *            レスポンスDTO
	 * @return 変換後のDTO
	 */
	private AgtRpaJobEndValueConditionInfoRequest convertDTO(AgtRpaJobEndValueConditionInfoResponse dto) {
		AgtRpaJobEndValueConditionInfoRequest endValueCondition = new AgtRpaJobEndValueConditionInfoRequest();
		endValueCondition.setOrderNo(dto.getOrderNo());
		endValueCondition.setConditionType(dto.getConditionType());
		endValueCondition.setPattern(dto.getPattern());
		endValueCondition.setCaseSensitivityFlg(dto.getCaseSensitivityFlg());
		endValueCondition.setProcessType(dto.getProcessType());
		endValueCondition.setReturnCode(dto.getReturnCode());
		endValueCondition.setReturnCodeCondition(dto.getReturnCodeCondition());
		endValueCondition.setUseCommandReturnCodeFlg(dto.getUseCommandReturnCodeFlg());
		endValueCondition.setEndValue(dto.getEndValue());
		endValueCondition.setDescription(dto.getDescription());
		return endValueCondition;
	}

	/**
	 * 終了値がスクリーンショット取得条件を満たしているかどうかを確認します。<br>
	 * 
	 * @param endValue
	 *            終了値
	 * @return true : スクリーンショットを取得する / false : スクリーンショットを取得しない
	 */
	private boolean checkScreenshotEndValueCondition(int endValue) {
		m_log.debug("checkScreenshotEndValueCondition() : endValue=" + endValue + ", endValueStr="
				+ m_info.getRpaScreenshotEndValue() + ", endValueCondition="
				+ m_info.getRpaScreenshotEndValueCondition());
		if (m_info.getRpaScreenshotEndValueCondition() == null) {
			m_log.warn("checkScreenshotEndValueCondition() : condition is null");
			return false;
		}
		Integer condition;
		// Enumを定数に変換
		switch (m_info.getRpaScreenshotEndValueCondition()) {
		case EQUAL_NUMERIC:
			condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
			break;
		case NOT_EQUAL_NUMERIC:
			condition = RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC;
			break;
		case GREATER_THAN:
			condition = RpaJobReturnCodeConditionConstant.GREATER_THAN;
			break;
		case GREATER_THAN_OR_EQUAL_TO:
			condition = RpaJobReturnCodeConditionConstant.GREATER_THAN_OR_EQUAL_TO;
			break;
		case LESS_THAN:
			condition = RpaJobReturnCodeConditionConstant.LESS_THAN;
			break;
		case LESS_THAN_OR_EQUAL_TO:
			condition = RpaJobReturnCodeConditionConstant.LESS_THAN_OR_EQUAL_TO;
			break;
		default:
			m_log.warn("checkScreenshotEndValueCondition() : unknown condition, "
					+ m_info.getRpaScreenshotEndValueCondition());
			condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		}
		
		boolean result = ReturnCodeConditionChecker.check(endValue, m_info.getRpaScreenshotEndValue(), condition);
		m_log.debug("checkScreenshotEndValueCondition() : result=" + result);
		return result;
	}

	/**
	 * 終了値から終了状態を判定し、ログアウトを行うかどうか確認します。<br>
	 * 異常発生時もログアウトするフラグがfalseの場合、終了状態が異常の場合はログアウトを行いません。
	 * 
	 * @param endValue
	 *            終了値
	 * @return true : ログアウトする / false : ログアウトしない
	 * @throws IOException
	 * @throws HinemosException 
	 */
	private boolean checkLogout(int endValue) throws IOException, HinemosException {
		// 終了状態を判定
		int endStatus = JobCommonUtil.checkEndStatus(endValue, m_info.getRpaNormalEndValueFrom(),
				m_info.getRpaNormalEndValueTo(), m_info.getRpaWarnEndValueFrom(), m_info.getRpaWarnEndValueTo());
		if(m_log.isDebugEnabled()){
			m_log.debug("checkLogout() : endValue=" + endValue + ", endStatus=" + endStatus + ", normalEndValueFrom="
					+ m_info.getRpaNormalEndValueFrom() + ", normalEndValueTo=" + m_info.getRpaNormalEndValueTo()
					+ ", warnEndValueFrom=" + m_info.getRpaWarnEndValueFrom() + ", warnEndValueTo="
					+ m_info.getRpaWarnEndValueTo());
		}
		if (endStatus == EndStatusConstant.TYPE_ABNORMAL) {
			// 異常終了の場合はログアウトする設定の場合のみログアウトする
			if (!m_info.getRpaRoboRunInfo().getLogout()) {
				m_log.info("checkLogout() : skip logout because of abnormal exit end status");
				return false;
			}
		}
		this.isLogoutStarted = true;
		// RPAツールエグゼキューターにログアウトを指示、一般ユーザでの実行可能性があるためEveryoneのフルアクセスで設定
		roboFileManager.writeWithEveryoneFullAccess(new RoboLogoutInfo(HinemosTime.currentTimeMillis(), m_info.getSessionId(),
				m_info.getJobunitId(), m_info.getJobId(), m_info.getFacilityId(), this.activeUserName));
		// 指示受付確認待ち
		boolean retLogoutInst = roboFileManager.confirmDelete(RoboLogoutInfo.class, true, instConfirmInterval,
				instConfirmTimeout);
		if (!retLogoutInst) {
			m_log.warn("checkLogout() : The logout instruction may not have been communicated to the executor. session="
					+ m_info.getSessionId());
		}

		// ログアウト完了待ち
		// ログアウト処理の完了時にRPAツールエグゼキューターも終了するため、RPAツールエグゼキューターの終了待ちも行う
		long limitMills = HinemosTime.currentTimeMillis() + logoutConfirmTimeout;
		while (true) {
			try {
				if (!(RpaWindowsUtil.hasActiveSession(this.activeUserName))
						&& RpaWindowsUtil.isRpaExecutorTerminate(this.activeUserName)) {
					m_log.debug("checkLogout() : isRpaExecutorTerminate = true. session=" + m_info.getSessionId());
					break;
				}
				Thread.sleep(logoutConfirmInterval);
			} catch (IOException | HinemosUnknown | InterruptedException e) {
				m_log.error("checkLogout() : An abnormality occurred when checking the status of the executor. session="
						+ m_info.getSessionId());
				break;
			}
			if (limitMills <= HinemosTime.currentTimeMillis()) {
				m_log.warn("checkLogout() :The logout instruction may not have ended at the executor. session="
						+ m_info.getSessionId());
				break;
			}
		}

		return true;
	}

	/**
	 * ログファイルによる終了値判定条件が設定されているかどうかを返します。
	 * 
	 * @return true : 設定あり / false : 設定なし
	 */
	private boolean isLogConditionExisting() {
		for (AgtRpaJobEndValueConditionInfoResponse condition : m_info.getRpaEndValueConditionInfoList()) {
			if (condition.getConditionType() == RpaJobEndValueConditionTypeConstant.LOG) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 実行結果ファイルの生成待ちを取りやめ、スレッドを終了させます。<br>
	 * ジョブが停止された場合に実行されます。
	 */
	public static void terminate(AgtRunInstructionInfoResponse info) {
		m_log.info("terminate()");
		String key = RunHistoryUtil.getKey(info);
		if (instances.containsKey(key)) {
			ScenarioMonitorThread instance = instances.get(key);
			instance.roboFileManager.abort();
			instance.interrupt(); // 待機を即時に終了する
		} else {
			m_log.warn("terminate() : thread not found");
		}
	}

	/**
	 * シナリオ監視スレッドが実行中かどうかを返します。
	 * 
	 * @param info
	 * @return true: 実行中、false: 実行中でない
	 */
	public static boolean isRunning(AgtRunInstructionInfoResponse info) {
		String key = RunHistoryUtil.getKey(info);
		return instances.containsKey(key);
	}

	/**
	 * 終了メッセージを送信します。<br>
	 * ジョブが停止された場合に呼ばれます。
	 */
	private void sendStopMessage() {
		JobResultSendableObject jobResult = new JobResultSendableObject();
		jobResult.sessionId = m_info.getSessionId();
		jobResult.jobunitId = m_info.getJobunitId();
		jobResult.jobId = m_info.getJobId();
		jobResult.facilityId = m_info.getFacilityId();
		jobResult.body = new SetJobResultRequest();
		jobResult.body.setCommand(m_info.getCommand());
		jobResult.body.setCommandType(CommandTypeConstant.STOP); // 正常終了した場合とメッセージを分けるためSTOPを指定
		jobResult.body.setStatus(RunStatusConstant.END);
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());

		m_log.info("sendJobEndMessage() : SessionID=" + jobResult.sessionId + ", JobID=" + jobResult.jobId);
		// 送信
		m_sendQueue.put(jobResult);
	}

	/**
	 * シナリオ監視スレッドがエグゼキューターへログアウトを指示したかどうかを返します。
	 * 
	 * @param info
	 * @return true: ログアウト指示済み、false: 指示無し
	 */
	public static boolean isLogoutStarted(AgtRunInstructionInfoResponse info) {
		String key = RunHistoryUtil.getKey(info);
		if (instances.containsKey(key)) {
			ScenarioMonitorThread instance = instances.get(key);
			return instance.isLogoutStarted;
		} else {
			m_log.warn("isLogoutStarted() : thread not found");
			return false;
		}
	}
}
