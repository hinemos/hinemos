/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.SetJobResultRequest;
import org.openapitools.client.model.SetJobResultRequest.RpaJobErrorTypeEnum;
import org.openapitools.client.model.SetJobStartRequest;
import org.openapitools.client.model.SetJobStartResponse;

import com.clustercontrol.agent.AgentRestClientWrapper;
import com.clustercontrol.agent.ReceiveTopic;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.job.AgentThread;
import com.clustercontrol.agent.job.RunHistoryUtil;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.agent.util.RestAgentBeanUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * RPAシナリオジョブの実行を開始するクラス。<br>
 * 以下をチェックし問題なければRPAシナリオを実行します。
 * <ul>
 * <li>ログインセッションがあること</li>
 * <li>RPAToolExecutorが起動していること</li>
 * <li>RPAツールが起動していないこと</li>
 * </ul>
 */
public class RpaScenarioThread extends AgentThread {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(RpaScenarioThread.class);

	/** 実行結果ファイルチェック間隔 */
	private static int checkInterval = 10000; // 10sec

	/** インスタンスを格納 */
	static private Map<String, RpaScenarioThread> instances = new ConcurrentHashMap<>();

	/** スレッドを停止するためのフラグ */
	private volatile boolean waiting = true;

	/** シナリオの同時実行を防ぐためのロック */
	private static final Lock lock = new ReentrantLock();

	/** シナリオエグゼキューター起動待ち（ミリ秒） */
	private static final int EXECUTOR_START_WAIT = 10000;

	/** シナリオ開始スレッド開始完了待ち（ミリ秒） */
	private static final int MONITOR_START_WAIT = 10000;


	static {
		try {
			String checkIntervalStr = AgentProperties.getProperty("job.rpa.result.check.interval",
					Integer.toString(checkInterval));
			checkInterval = Integer.parseInt(checkIntervalStr);
		} catch (Exception e) {
			m_log.warn("ScenarioMonitorThread : " + e.getMessage(), e);
		}
		m_log.info("checkInterval=" + checkInterval);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param info
	 * @param sendQueue
	 */
	public RpaScenarioThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue) {
		super(info, sendQueue);
		this.setName(this.getClass().getSimpleName());
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
		m_log.info("run() : start");
		try {
			// 別のRpaScenarioThreadが実行中の場合、終了するまで待機
			// マネージャ側で同時実行しないよう制御しているので念のための処理
			lock.lock();
			m_log.debug("run() : lock");
			execute();
		} catch (InterruptedException e) {
			// 中断された場合
			m_log.debug("run() : interrupted. e=" + e.getMessage(), e);
		} finally {
			lock.unlock();
			m_log.debug("run() : unlock");
		}
		m_log.info("run() : end");
	}

	/**
	 * 確認を行い問題なければRPAシナリオ実行を開始します。
	 * <ol>
	 * <li>ログインセッションが存在すること</li>
	 * <li>RPAツールエグゼキューターが起動していること</li>
	 * <li>RPAツールのプロセスが存在しないこと</li>
	 * </ol>
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	private void execute() throws InterruptedException {
		long start = HinemosTime.currentTimeMillis();

		// 開始メッセージ送信
		if (!sendStartMessage()) {
			return;
		}
		m_log.debug("execute() : start. m_info=" + m_info);

		// 実行履歴に追加
		RunHistoryUtil.addRunHistory(m_info, RunHistoryUtil.dummyProcess());

		// ジョブ停止時に使用できるようインスタンスをマップに格納
		instances.put(RunHistoryUtil.getKey(m_info), this);

		String username = null;
		while (waiting) {
			// ファイルが存在しない場合処理を終了
			if(!checkFilePath(m_info)){
				sendErrorMessage(RpaJobErrorTypeEnum.FILE_DOES_NOT_EXIST);
				return;
			};

			boolean rpaExecutorRunnning = false;
			boolean rpaRunning = false;
			try {
				// アクティブなユーザを取得
				List<String> users = RpaWindowsUtil.getActiveUsers();
				if (users == null || users.size() <= 0) {
					// （ログインフラグなしの場合は m_info.getRpaLoginWaitMills() は 0 となる）
					if (m_info.getRpaLoginWaitMills() <= 0) {
						// ログインフラグなしの場合で、ログインされていない
						sendErrorMessage(RpaJobErrorTypeEnum.NOT_LOGIN);
						m_log.warn("execute() : not login, end");
						return;
					} else {
						// ログインフラグありの場合で、ログイン失敗
						long elapsedTime = HinemosTime.currentTimeMillis() - start;
						m_log.debug("execute() : waiting " + elapsedTime + "ms for login until " + m_info.getRpaLoginWaitMills() + "ms");
						if (elapsedTime > m_info.getRpaLoginWaitMills()) {
							// 待機時間を超えた場合は終了する
							sendErrorMessage(RpaJobErrorTypeEnum.LOGIN_ERROR);
							m_log.warn("execute() : login failed, waiting " + elapsedTime + "ms for login over " + m_info.getRpaLoginWaitMills() + "ms");
							return;
						}
						Thread.sleep(checkInterval);
						continue;
					}
				} else if (users.size() >= 2) {
					// 複数ログインされている場合、エラー
					sendErrorMessage(RpaJobErrorTypeEnum.TOO_MANY_LOGIN_SESSION);
					m_log.warn("execute() : active session is too much. users=" + Arrays.toString(users.toArray()));
					return;
				}
				username = users.get(0);

				// RPAツールエグゼキューターの起動チェック
				rpaExecutorRunnning = RpaWindowsUtil.isRpaExecutorRunnning(username);
				if (!rpaExecutorRunnning) {
					// 待機時間を超えた場合は終了する
					long elapsedTime = HinemosTime.currentTimeMillis() - start;
					long waitMills = EXECUTOR_START_WAIT + m_info.getRpaLoginWaitMills();
					m_log.debug("execute() : waiting " + elapsedTime + "ms for starting executor until " + waitMills + "ms");
					if (elapsedTime > waitMills) {
						// ログインされていない（RPAツールエグゼキューターが起動していない）
						sendErrorMessage(RpaJobErrorTypeEnum.NOT_RUNNING_EXECUTOR);
						m_log.warn("execute() : scenario executor not running, end");
						return;
					}
					Thread.sleep(checkInterval);
					continue;
				}

				// RPAツールプロセス存在チェック
				// 実行ファイルパスから拡張子を除く
				String processName = StringUtils.substringBeforeLast(m_info.getRpaExeName(), ".");
				rpaRunning = RpaWindowsUtil.existsProcessName(processName);
				if (rpaRunning) {
					// RPAツールが既に動作している場合は終了
					sendErrorMessage(RpaJobErrorTypeEnum.ALREADY_RUNNING);
					m_log.info("execute() : rpa already running, end");
					return;
				}
			} catch (IOException | HinemosUnknown e) {
				sendErrorMessage(RpaJobErrorTypeEnum.ERROR_OCCURRED);
				m_log.error("execute() : error occurred. e=" + e.getMessage(), e);
			}

			// 問題なければRPAシナリオを実行
			if (username != null && rpaExecutorRunnning && !rpaRunning) {
				// シナリオ監視スレッドの開始
				ScenarioMonitorThread scenarioMonitorThread = new ScenarioMonitorThread(m_info, m_sendQueue, username);
				scenarioMonitorThread.start();
				// シナリオ監視スレッド 開始完了待ち
				// (非同期でスタートしてるので ScenarioMonitorThread.instancesへの登録完了まで若干のwaitが必要)
				Thread.sleep(MONITOR_START_WAIT);
				break;
			}

			Thread.sleep(checkInterval);
		}

		// RPAシナリオ実行中の定期確認
		while (waiting) {
			// シナリオ監視スレッドが終了したら処理を終了
			if (!ScenarioMonitorThread.isRunning(m_info)) {
				return;
			}
			// ScenarioMonitorThread がセッションログアウトを始めていてたら、各種死活チェックは不要
			if (ScenarioMonitorThread.isLogoutStarted(m_info)) {
				Thread.sleep(checkInterval);
				continue;
			}

			// ログインセッションのチェック
			boolean hasActiveSession = false;
			try {
				hasActiveSession = RpaWindowsUtil.hasActiveSession(username);
			} catch (HinemosUnknown e) {
				m_log.warn("execute() : error occurred. e=" + e.getMessage(), e);
			}
			// シナリオ監視スレッドが終了したら処理を終了
			// タイミングがシビアなので再度チェック
			if (!ScenarioMonitorThread.isRunning(m_info)) {
				return;
			}
			if (!ScenarioMonitorThread.isLogoutStarted(m_info)	// タイミングがシビアなので再度チェック
					&& !hasActiveSession) {
				sendErrorMessage(RpaJobErrorTypeEnum.LOST_LOGIN_SESSION);
				m_log.warn("execute() : lost login session, end");
				ScenarioMonitorThread.terminate(m_info); // シナリオ監視スレッドを終了
				return;
			}

			// RPAツールエグゼキューターの死活チェック
			boolean isRpaExecutorRunnning = false;
			try {
				isRpaExecutorRunnning = RpaWindowsUtil.isRpaExecutorRunnning(username);
			} catch (IOException | HinemosUnknown e) {
				m_log.warn("execute() : error occurred. e=" + e.getMessage(), e);
			}
			// シナリオ監視スレッドが終了したら処理を終了
			// タイミングがシビアなので再度チェック
			if (!ScenarioMonitorThread.isRunning(m_info)) {
				return;
			}
			if (!ScenarioMonitorThread.isLogoutStarted(m_info)	// タイミングがシビアなので再度チェック
					&& !isRpaExecutorRunnning) {
				sendErrorMessage(RpaJobErrorTypeEnum.ABNORMAL_EXIT);
				m_log.warn("execute() : rpa scenario executor process exited abnormally, end");
				ScenarioMonitorThread.terminate(m_info); // シナリオ監視スレッドを終了
				return;
			}

			Thread.sleep(checkInterval);
		}
	}

	/**
	 * 指定したパスがファイルであることを確認します。
	 *
	 * @return ファイル有無
	 */
	private boolean checkFilePath(AgtRunInstructionInfoResponse info) {
		if (info == null || info.getFilePath() == null) {
			m_log.warn("checkFilePath() : info is null or FilePath is null. info=" + info);
			return false;
		}
		File filePath = new File(info.getFilePath());
		if (filePath.isFile()) {
			m_log.debug("checkFilePath() : FilePath is file. info.getFilePath()=" + info.getFilePath());
			return true;
		}

		m_log.warn("checkFilePath() : FilePath is NOT file. info=" + info);
		return false;
	}

	/**
	 * 待機フラグをセットします。
	 * 
	 * @param waiting
	 *            待機フラグ
	 */
	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	/**
	 * スレッドを停止します。
	 * 
	 * @param info
	 *            ジョブ実行指示情報
	 */
	public static void terminate(AgtRunInstructionInfoResponse info) {
		m_log.info("terminate()");
		String key = RunHistoryUtil.getKey(info);
		if (instances.containsKey(key)) {
			RpaScenarioThread instance = instances.get(key);
			instance.setWaiting(false);
			instance.interrupt(); // 待機を即時に終了する
			// シナリオ監視スレッドを停止
			ScenarioMonitorThread.terminate(info);
		} else {
			m_log.warn("terminate() : thread not found");
		}
	}

	/**
	 * 開始メッセージを送信します。
	 * 
	 * @return ジョブがすでに起動している場合falseを返します。
	 */
	private boolean sendStartMessage() {
		JobResultSendableObject jobResult = new JobResultSendableObject();
		jobResult.sessionId = m_info.getSessionId();
		jobResult.jobunitId = m_info.getJobunitId();
		jobResult.jobId = m_info.getJobId();
		jobResult.facilityId = m_info.getFacilityId();
		jobResult.body = new SetJobResultRequest();
		jobResult.body.setCommand(m_info.getCommand());
		jobResult.body.setCommandType(m_info.getCommandType());
		jobResult.body.setStopType(m_info.getStopType());
		jobResult.body.setStatus(RunStatusConstant.START);
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
		SetJobStartRequest setJobStartRequest = new SetJobStartRequest();
		try {
			RestAgentBeanUtil.convertBeanSimple(jobResult.body, setJobStartRequest);
		} catch (HinemosUnknown e) {
			m_log.error("sendJobStartMessage() : " + e.getMessage(), e);
			return false;
		}

		m_log.info("sendJobStartMessage() : SessionID=" + jobResult.sessionId + ", JobID=" + jobResult.jobId);

		// Hinemosマネージャに開始メッセージ送信
		// マネージャに開始メッセージが届く前にジョブのコマンドが実行されることと
		// VIPの切り替えが起こった場合に、ジョブが複数のエージェントで起動することを防ぐために
		// ジョブの開始報告は同期した動作とする
		AgentRequestId agentRequestId = new AgentRequestId();

		while (!ReceiveTopic.isHistoryClear()) {
			try {
				SetJobStartResponse res = AgentRestClientWrapper.setJobStart(jobResult.sessionId, jobResult.jobunitId,
						jobResult.jobId, jobResult.facilityId, setJobStartRequest,
						agentRequestId.toRequestHeaderValue());

				if (res == null) {
					// setJobStartがマネージャ側で重複した場合
					// 異常終了の旨をメッセージ送信
					m_log.warn("Agent Request ID is duplicated. Job terminated abnormally. SessionID="
							+ jobResult.sessionId + ", JobID=" + jobResult.jobId);
					sendErrorMessage(SetJobResultRequest.RpaJobErrorTypeEnum.OTHER, "Agent Request ID is duplicated.");
					return false;
				}

				if (!res.getJobRunnable().booleanValue()) {
					// ジョブがすでに起動している場合
					m_log.warn("This job already run by other agent. SessionID=" + jobResult.sessionId + ", JobID="
							+ jobResult.jobId);
					return false;
				}
				return true;
			} catch (Exception e) {
				m_log.error("sendJobStartMessage() : " + e.getMessage(), e);
				return false;
			}
		}
		return true;
	}

	/**
	 * エラーメッセージを送信します。
	 * 
	 * @param errorType
	 * @param errorMessage
	 */
	private void sendErrorMessage(SetJobResultRequest.RpaJobErrorTypeEnum errorType, String errorMessage) {
		JobResultSendableObject jobResult = new JobResultSendableObject();
		jobResult.sessionId = m_info.getSessionId();
		jobResult.jobunitId = m_info.getJobunitId();
		jobResult.jobId = m_info.getJobId();
		jobResult.facilityId = m_info.getFacilityId();
		jobResult.body = new SetJobResultRequest();
		jobResult.body.setCommand(m_info.getCommand());
		jobResult.body.setCommandType(m_info.getCommandType());
		jobResult.body.setStatus(RunStatusConstant.ERROR);
		jobResult.body.setRpaJobErrorType(errorType);
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
		jobResult.body.setErrorMessage(errorMessage);

		m_log.info("sendJobErrorMessage() : SessionID=" + jobResult.sessionId + ", JobID=" + jobResult.jobId
				+ ", ErrorType=" + errorType);
		// 送信
		m_sendQueue.put(jobResult);
	}

	/**
	 * エラーメッセージを送信します。
	 * 
	 * @param errorType
	 */
	private void sendErrorMessage(SetJobResultRequest.RpaJobErrorTypeEnum errorType) {
		sendErrorMessage(errorType, "");
	}

}
