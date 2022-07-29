/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionFileCheckInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.SetJobResultFileCheckRequest;
import org.openapitools.client.model.SetJobResultRequest;
import org.openapitools.client.model.SetJobStartRequest;
import org.openapitools.client.model.SetJobStartResponse;

import com.clustercontrol.agent.AgentRestClientWrapper;
import com.clustercontrol.agent.ReceiveTopic;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.filecheck.FileCheck;
import com.clustercontrol.agent.filecheck.FileCheckInfo;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.agent.util.RestAgentBeanUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.SessionIdLocked;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブによるファイルチェックを実行するスレッドクラス<BR>
 *
 * 一つのジョブの実行指示に対し、一つのスレッドが生成されます。<BR>
 */
public class FileCheckJobThread extends AgentThread {
	private static Log logger = LogFactory.getLog(FileCheckJobThread.class);

	private JobResultSendableObject jobResult;

	/** ファイルチェック間隔 */
	private int runInterval = 10000; // Default 10sec
	/** FileCheckInfoのkeyとして利用する文字列 */
	private static final String FILECHECKJOB_KEY = "FileCheckJob";
	/** 実行フラグ */
	private volatile boolean run = true;
	/** 停止指示実行情報 */
	private AgtRunInstructionInfoResponse stopInstruction = null;

	// タイムアウト以外で停止した場合の終了値（ユーザの設定値ではないため固定値とする）
	private static final int END_VALUE_FORCE_STOP = 9;
	// 異常終了した場合の終了値
	private static final int END_VALUE_ERROR = -1;

	// Hinemosマネージャに開始メッセージ送信のリトライ間隔(ミリ秒)
	private long interval = Long.parseLong(AgentProperties.getProperty("job.reconnection.interval"));

	/**
	 * コンストラクタ
	 * 
	 * @param info
	 * @param sendQueue
	 */
	public FileCheckJobThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue) {
		super(info, sendQueue);

		// メッセージ送信用
		jobResult = new JobResultSendableObject();
		jobResult.sessionId = m_info.getSessionId();
		jobResult.jobunitId = m_info.getJobunitId();
		jobResult.jobId = m_info.getJobId();
		jobResult.facilityId = m_info.getFacilityId();
		jobResult.body = new SetJobResultRequest();

		if (m_info.getCommandType() == CommandTypeConstant.STOP
				&& m_info.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
			logger.info("runJob() : runHistory = null, FileCheck is already stopped.");
			// 実行履歴が存在しない場合にここに到達する
			// 判定が通過と同時に停止指示が送られてきた場合など、タイミング次第で到達する
			// 他のノードで条件を満たした場合やタイムアウトを考慮しエラーとはしない
			// cf.) DeleteProcessThread
			jobResult.body.setCommand(m_info.getCommand());
			jobResult.body.setCommandType(m_info.getCommandType());
			jobResult.body.setStopType(m_info.getStopType());
			jobResult.body.setStatus(RunStatusConstant.END);
			jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
			jobResult.body.setEndValue(END_VALUE_FORCE_STOP);

			// 結果を送信して終了する
			m_sendQueue.put(jobResult);
			run = false;
			return;
		}

		try {
			// 実行間隔
			String runIntervalStr = AgentProperties.getProperty("job.filecheck.interval",
					Integer.toString(runInterval));
			runInterval = Integer.parseInt(runIntervalStr);
		} catch (Exception e) {
			logger.warn("FileCheckJobThread() : " + e.getMessage());
		}

		// ---------------------------
		// -- 開始メッセージ送信
		// ---------------------------

		// メッセージ作成
		jobResult.body.setCommand(m_info.getCommand());
		jobResult.body.setCommandType(m_info.getCommandType());
		jobResult.body.setStopType(m_info.getStopType());
		jobResult.body.setStatus(RunStatusConstant.START);
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
		SetJobStartRequest setJobStartRequest = new SetJobStartRequest();
		try {
			RestAgentBeanUtil.convertBeanSimple(jobResult.body, setJobStartRequest);
		} catch (HinemosUnknown e) {
			logger.error("FileCheckJobThread() : " + e.getMessage(), e);
			return;
		}

		logger.info("FileCheckJobThread() : run SessionID=" + jobResult.sessionId + ", JobID=" + jobResult.jobId);

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
					JobResultSendableObject snd = new JobResultSendableObject();
					snd.sessionId = m_info.getSessionId();
					snd.jobunitId = m_info.getJobunitId();
					snd.jobId = m_info.getJobId();
					snd.facilityId = m_info.getFacilityId();
					snd.body = new SetJobResultRequest();
					snd.body.setCommand(m_info.getCommand());
					snd.body.setCommandType(m_info.getCommandType());
					snd.body.setStopType(m_info.getStopType());
					snd.body.setStatus(RunStatusConstant.ERROR);
					snd.body.setTime(HinemosTime.getDateInstance().getTime());
					snd.body.setErrorMessage("Agent Request ID is duplicated.");
					snd.body.setMessage("");
					m_sendQueue.put(snd);

					logger.warn("Agent Request ID is duplicated. Job terminated abnormally. SessionID="
							+ jobResult.sessionId + ", JobID=" + jobResult.jobId);
					run = false;
					return;
				}
				if (!res.getJobRunnable().booleanValue()) {
					// ジョブがすでに起動している場合
					logger.warn("This job already run by other agent. SessionID=" + jobResult.sessionId + ", JobID="
							+ jobResult.jobId);
					run = false;
					return;
				}

				break;
			} catch (SessionIdLocked e) {
				// 開始メッセージがリジェクトされた場合、一定時間後リトライ
				try {
					logger.warn("Rejected because sessionID is locked. setJobStart retry after " + interval + "[ms]. "
							+ "SessionID=" + jobResult.sessionId + ", JobID=" + jobResult.jobId);
					Thread.sleep(interval);
				} catch (InterruptedException e1) {
					logger.error("FileCheckJobThread() : " + e.getMessage(), e);
					run = false;
					return;
				}
			} catch (Exception e) {
				logger.error("FileCheckJobThread() : " + e.getMessage(), e);
				run = false;
				return;
			}
		}

		// 実行履歴に追加
		RunHistoryUtil.addRunHistory(m_info, this);
	}

	@Override
	public void run() {
		if (!run) {
			return;
		}
		logger.info("run() : FileCheckJobThread");

		AgtRunInstructionFileCheckInfoResponse fileCheckInfoRes = m_info.getRunInstructionFileCheckInfo();
		if (fileCheckInfoRes == null) {
			// 通常起こりえない
			logger.error("run() : RunFileCheckInfo is null. SessionID=" + m_info.getSessionId() + ", JobID="
					+ m_info.getJobId());
			return;
		}
		FileCheckInfo fileCheckInfo = new FileCheckInfo(FILECHECKJOB_KEY); // ジョブは1:1なのでkeyは固定値とする
		fileCheckInfo.setFileNamePattern(fileCheckInfoRes.getFileName());
		fileCheckInfo.setCreateValidFlg(fileCheckInfoRes.getCreateValidFlg());
		fileCheckInfo.setCreateBeforeInitFlg(fileCheckInfoRes.getCreateBeforeJobStartFlg());
		fileCheckInfo.setModifyValidFlg(fileCheckInfoRes.getModifyValidFlg());
		fileCheckInfo.setDeleteValidFlg(fileCheckInfoRes.getDeleteValidFlg());
		fileCheckInfo.setModifyType(fileCheckInfoRes.getModifyType());
		fileCheckInfo.setNotJudgeFileInUseFlg(fileCheckInfoRes.getNotJudgeFileInUseFlg());

		FileCheck filecheck = new FileCheck(fileCheckInfoRes.getDirectory(), true, fileCheckInfo);

		while (run) {
			try {
				// ファイルチェック実行
				filecheck.run();

				if (filecheck.isPassed() || !run) {
					break;
				}

			} catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
				String msg = "Failed to exec. FileCheckJobThread is terminated. " + e.getClass().getCanonicalName()
						+ ", " + e.getMessage();
				logger.error("run() : " + msg, e);
				// Windows版でライブラリに異常があった場合に到達する可能性がある
				// エージェントを再起動しないと回復しない（再起動も確実というわけではないが）ため、
				// ファイルチェック実行契機同様にINTERNALイベントを通知してジョブを終了する
				sendMessage(PriorityConstant.TYPE_CRITICAL,
						MessageConstant.MESSAGE_JOBFILECHECK_FAILED_TO_CHECK.getMessage(), msg);
				// 結果を送信
				jobResult.body.setStatus(RunStatusConstant.ERROR);
				jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
				jobResult.body.setEndValue(END_VALUE_ERROR);
				jobResult.body.setMessage("");
				jobResult.body.setErrorMessage(msg);
				m_sendQueue.put(jobResult);
				// 実行履歴から削除
				RunHistoryUtil.delRunHistory(m_info);
				return;
			} catch (Exception e) {
				logger.warn("run() : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			} catch (Throwable e) {
				logger.error("run() : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			}
			try {
				// 実行間隔分待機する
				Thread.sleep(runInterval);
			} catch (InterruptedException e) {
				logger.info("run() : FileCheckJobThread is Interrupted");
				break;
			}
		}

		if (filecheck.isPassed() && run) {
			// 終了を送信
			jobResult.body.setStatus(RunStatusConstant.END);
			jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
			jobResult.body.setEndValue(fileCheckInfoRes.getSuccessEndValue());
			String passedEventTypeStr = FileCheckConstant
					.resultTypeToMessage(filecheck.getPassedFileCheck().getPassedEventType());
			jobResult.body.setMessage(MessageConstant.FILE_CHECK_FINISHED.getMessage(passedEventTypeStr,
					fileCheckInfoRes.getDirectory(), filecheck.getPassedFileCheck().getPassedFileName()));
			jobResult.body.setErrorMessage("");

			SetJobResultFileCheckRequest resultFileCheckInfo = new SetJobResultFileCheckRequest();
			resultFileCheckInfo.setDirectory(fileCheckInfoRes.getDirectory());
			resultFileCheckInfo.setFileName(filecheck.getPassedFileCheck().getPassedFileName());
			resultFileCheckInfo.setPassedEventType(filecheck.getPassedFileCheck().getPassedEventType());
			resultFileCheckInfo.setFileTimestamp(filecheck.getPassedFileCheck().getFileTimestamp());
			resultFileCheckInfo.setFileSize(filecheck.getPassedFileCheck().getFileSize());
			jobResult.body.setRunResultFileCheckInfo(resultFileCheckInfo);

			// 実行履歴から削除
			RunHistoryUtil.delRunHistory(m_info);
		} else {
			// ジョブが停止された場合はここを通る
			AgtRunInstructionInfoResponse stopInstruction;
			Integer endStatus;
			if (this.stopInstruction == null) {
				// スレッド停止などの特殊な例外が発生しない限り通常到達しない
				logger.warn("run() : stopInstruction is null.");
				stopInstruction = m_info;
				endStatus = RunStatusConstant.ERROR;
			} else {
				stopInstruction = this.stopInstruction;
				endStatus = RunStatusConstant.END;
			}

			logger.info("run() : FileCheckJob stopped. command=" + stopInstruction.getCommand() + ", SessionID="
					+ stopInstruction.getSessionId() + ", JobID=" + stopInstruction.getJobId());

			jobResult.body.setCommand(stopInstruction.getCommand());
			jobResult.body.setCommandType(stopInstruction.getCommandType());
			jobResult.body.setStopType(stopInstruction.getStopType());
			jobResult.body.setStatus(endStatus);
			jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
			jobResult.body.setEndValue(END_VALUE_FORCE_STOP);
		}

		// 結果を送信
		m_sendQueue.put(jobResult);

		logger.debug("run() : end");
	}

	/**
	 * 停止処理
	 * 
	 * @param info
	 */
	public void terminate(AgtRunInstructionInfoResponse info) {
		stopInstruction = info;
		run = false;

		// スレッドが停止するまでに同じジョブが再実行されるケースを考慮し、
		// マネージャから停止指示が来た場合はこの時点で実行履歴から削除する
		RunHistoryUtil.delRunHistory(m_info);
	}

	/**
	 * INTERNALイベント通知をマネージャに送信する。<BR>
	 * 
	 * @param priority
	 * @param message
	 * @param messageOrg
	 */
	private void sendMessage(int priority, String message, String messageOrg) {
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.JOB);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(MessageConstant.AGENT.getMessage());
		sendme.body.setMessage(message);
		sendme.body.setMessageOrg(messageOrg);
		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(HinemosModuleConstant.SYSYTEM);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。
		m_sendQueue.put(sendme);
	}
}
