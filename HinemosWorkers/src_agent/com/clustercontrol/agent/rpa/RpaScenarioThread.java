/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentRestClientWrapper;
import com.clustercontrol.agent.ReceiveTopic;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.job.AgentThread;
import com.clustercontrol.agent.job.RunHistoryUtil;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.rpa.util.CommandProxy;
import com.clustercontrol.util.CommandExecutor.CommandResult;
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
	/** スレッド名 */
	private String threadName = "RpaScenarioThread";
	/** PIDファイル名 */
	private static final String PID_FILE_NAME = "_pid_rpa_scenario_executor";
	/** RPAツールエグゼキューター連携用ファイル出力先フォルダ */
	private String roboFileDir = Agent.getAgentHome() + "var/rpa";
	/** RPAツールエグゼキュータープロセス確認用コマンド */
	private String checkExecutorProcessCommand = "powershell.exe -Command Get-Process -Id %s";
	/** RPAツールプロセス確認用コマンド */
	private String checkRpaProcessCommand = "powershell.exe -Command Get-Process -Name %s";
	/** ログインセッション確認用コマンド */
	private String checkSessionCommand = "cmd /c \"quser | findstr Active\"";
	/** 実行結果ファイルチェック間隔 */
	private static int checkInterval = 10000; // 10sec
	/** インスタンスを格納 */
	static private Map<String, RpaScenarioThread> instances = new ConcurrentHashMap<>();
	/** スレッドを停止するためのフラグ */
	private volatile boolean waiting = true;
	/** シナリオの同時実行を防ぐためのロック */
	private static final Lock lock = new ReentrantLock();

	/**
	 * 確認結果
	 */
	private enum CheckResult {
		OK, NG, UNKNOWN
	}

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
		m_log.info("run() : start");
		try {
			// 別のRpaScenarioThreadが実行中の場合、終了するまで待機
			// マネージャ側で同時実行しないよう制御しているので念のための処理
			lock.lock();
			m_log.debug("run() : lock");
			execute();
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
	 */
	private void execute() {
		long start = HinemosTime.currentTimeMillis();

		// 開始メッセージ送信
		if (!sendStartMessage()) {
			return;
		}

		// 実行履歴に追加
		RunHistoryUtil.addRunHistory(m_info, RunHistoryUtil.dummyProcess());

		// ジョブ停止時に使用できるようインスタンスをマップに格納
		instances.put(RunHistoryUtil.getKey(m_info), this);

		while (waiting) {
			// ログインセッションが存在することをチェック
			CheckResult sessionActive = checkSessionActive();
			// RPAツールエグゼキューターの起動チェック
			CheckResult rpaExecutorRunnning = checkRpaExecutorRunnning();
			if (sessionActive == CheckResult.NG || rpaExecutorRunnning == CheckResult.NG) {
				// 待機時間を超えた場合は終了する
				long waitMills = HinemosTime.currentTimeMillis() - start;
				m_log.debug("execute() : waiting " + waitMills + "ms for login until " + m_info.getRpaLoginWaitMills()
						+ "ms");
				if (waitMills > m_info.getRpaLoginWaitMills()) {
					// ログインセッションが存在しない、
					// またはRPAツールエグゼキューターが起動していない
					sendErrorMessage(RpaJobErrorTypeEnum.NOT_LOGIN);
					m_log.info("execute() : not login, end");
					return; // 処理を終了
				}
			}

			// RPAツールプロセス存在チェック
			CheckResult rpaNotRunning = checkRpaNotRunning();
			if (rpaNotRunning == CheckResult.NG) {
				// RPAツールが既に動作している
				sendErrorMessage(RpaJobErrorTypeEnum.ALREADY_RUNNING);
				m_log.info("execute() : rpa already running, end");
				return; // 処理を終了
			}

			// 問題なければRPAシナリオを実行
			if (sessionActive == CheckResult.OK && rpaExecutorRunnning == CheckResult.OK
					&& rpaNotRunning == CheckResult.OK) {
				// シナリオ監視スレッドの開始
				ScenarioMonitorThread scenarioMonitorThread = new ScenarioMonitorThread(m_info, m_sendQueue);
				scenarioMonitorThread.start();
				break;
			}

			try {
				m_log.debug("execute() : not login / rpa already running check, sleep " + checkInterval + "ms");
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				m_log.warn("execute() : thread interrupted");
			}
		}

		// RPAシナリオ実行中の定期確認
		while (waiting) {
			// ログインセッションのチェック
			CheckResult sessionActive = checkSessionActive();
			// RPAツールエグゼキューターの死活チェック
			CheckResult rpaExecutorRunnning = checkRpaExecutorRunnning();
			if (sessionActive == CheckResult.NG || rpaExecutorRunnning == CheckResult.NG) {
				// 異常発生を通知
				sendErrorMessage(RpaJobErrorTypeEnum.ABNORMAL_EXIT);
				m_log.info("execute() : rpa tool executor process exited abnormally, end");
				ScenarioMonitorThread.terminate(m_info); // シナリオ監視スレッドを終了
				return; // 処理を終了
			}
			// シナリオ監視スレッドが終了したら処理を終了
			if (!ScenarioMonitorThread.isRunning(m_info)) {
				return;
			}
			try {
				m_log.debug("execute(): rpa executor process check, sleep " + checkInterval + "ms");
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				m_log.warn("execute() : thread interrupted");
			}
		}

		// ジョブが停止された場合
		if (!waiting) {
			sendStopMessage();
			return;
		}
	}

	/**
	 * RPAエグゼキュータープロセスが起動しているかどうか確認します。
	 * 
	 * @return OK: プロセスが起動している / NG: プロセスが起動していない
	 */
	private CheckResult checkRpaExecutorRunnning() {
		File pidFile = new File(roboFileDir, PID_FILE_NAME);
		if (!pidFile.exists()) {
			return CheckResult.NG; // pidファイルが存在しない
		}
		try {
			String pid = Files.readAllLines(pidFile.toPath(), StandardCharsets.UTF_8).get(0);
			String cmd = String.format(checkExecutorProcessCommand, pid);
			m_log.debug("checkRpaExecutorRunning() : cmd=" + cmd);
			CommandResult result = CommandProxy.execute(cmd);
			if (result != null) {
				m_log.debug("checkRpaExecutorRunning() : exitCode=" + result.exitCode);
				if (result.exitCode == 0) {
					return CheckResult.OK; // RPAツールエグゼキューターが起動中
				}
			} else {
				m_log.warn("checkRpaExecutorRunning() : result is null");
				// コマンド実行中にinterruptされた場合
				return CheckResult.UNKNOWN;
			}

		} catch (IOException e) {
			m_log.error("checkRpaExecutorRunnning() : read pid file failed, " + e.getMessage(), e);
		} catch (HinemosUnknown e) {
			m_log.error("checkRpaExecutorRunnning() : command execution failed, " + e.getMessage(), e);
		}
		return CheckResult.NG;
	}

	/**
	 * RPAツールのプロセスが起動していないことを確認します。
	 * 
	 * @return OK: プロセスが起動していない / NG: プロセスが起動している
	 */
	private CheckResult checkRpaNotRunning() {
		try {
			// 実行ファイルパスから拡張子を除く
			String processName = StringUtils.substringBeforeLast(m_info.getRpaExeName(), ".");
			String cmd = String.format(checkRpaProcessCommand, processName);
			CommandResult result = CommandProxy.execute(cmd);
			if (result != null) {
				m_log.debug("checkRpaNotRunning() : exitCode=" + result.exitCode);
				if (result.exitCode != 0) {
					return CheckResult.OK; // RPAツールが起動していない
				}
			} else {
				m_log.warn("checkRpaNotRunning() : result is null");
				// コマンド実行中にinterruptされた場合
				return CheckResult.UNKNOWN;
			}
		} catch (HinemosUnknown e) {
			m_log.error("checkRpaNotRunning() : command execution failed, " + e.getMessage(), e);
		}
		return CheckResult.NG;
	}

	/**
	 * ログインセッションがあることを確認します。
	 * 
	 * @return OK: ログインセッションがある / NG: ログインセッションが無い
	 */
	private CheckResult checkSessionActive() {
		try {
			CommandResult result = CommandProxy.execute(checkSessionCommand);
			if (result != null) {
				m_log.debug("checkActiveSession() : exitCode=" + result.exitCode);
				if (result.exitCode == 0) {
					return CheckResult.OK; // ログインセッションがある
				}
			} else {
				m_log.warn("checkActiveSession() : result is null");
				// コマンド実行中にinterruptされた場合
				return CheckResult.UNKNOWN;
			}
		} catch (HinemosUnknown e) {
			m_log.error("checkActiveSession() : command execution failed, " + e.getMessage(), e);
		}
		return CheckResult.NG;
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
