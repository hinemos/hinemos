/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkSendReturnValue;
import com.clustercontrol.jobmanagement.bean.MonitorJobConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageResponse;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * HinemosManager上でジョブ連携送信ジョブを実行するクラス<BR>
 */
public class JobLinkSendJobWorker {

	// Logger
	private static Log m_log = LogFactory.getLog(JobLinkSendJobWorker.class);

	private static ExecutorService service;
	private static String workerName = "JobLinkSendJobWorker";

	static {
		int maxThreadPoolSize = HinemosPropertyCommon.joblink_send_job_thread_pool_size.getIntegerValue();

		service = new MonitoredThreadPoolExecutor(maxThreadPoolSize, maxThreadPoolSize, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, workerName + "-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());
	}

	/**
	 * ジョブ実行処理
	 * 
	 * @param runInstructionInfo
	 *            実行指示
	 */
	public static void runJob(RunInstructionInfo runInstructionInfo) {

		m_log.info("runJob() SessionID=" + runInstructionInfo.getSessionId() + ", JobunitID="
				+ runInstructionInfo.getJobunitId() + ", JobID=" + runInstructionInfo.getJobId() + ", FacilityID="
				+ runInstructionInfo.getFacilityId() + ", CommandType=" + runInstructionInfo.getCommandType());
		try {
			// タスクを実行する
			service.execute(new JobLinkSendTask(runInstructionInfo));
		} catch (Throwable e) {
			m_log.warn("runJob() Error : " + e.getMessage());
		}
	}

	/**
	 * ジョブ連携送信ジョブを終了する。
	 * 
	 * @param runInstructionInfo
	 *            実行指示
	 * @param response
	 *            実行結果
	 * @param errorMessage
	 *            エラーメッセージ
	 * @param status
	 *            ステータス
	 * @param isSuccess
	 *            true:送信成功、false:送信失敗
	 */
	public static void endJobLinkSendJob(RunInstructionInfo runInstructionInfo, RegistJobLinkMessageResponse response,
			String errorMessage, Integer status, boolean isSuccess) {

		m_log.debug("endJobLinkSendJob() : sessionId=" + runInstructionInfo.getSessionId() + ", JobunitId="
				+ runInstructionInfo.getJobunitId() + ", JobId=" + runInstructionInfo.getJobId());

		Integer endValue = 0;
		if (isSuccess) {
			endValue = JobLinkSendReturnValue.SUCCESS.value();
		} else {
			endValue = JobLinkSendReturnValue.FAILURE.value();
		}
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 履歴削除
			RunHistoryUtil.delRunHistory(runInstructionInfo);

			// メッセージ送信
			String message = "";
			if (response != null) {
				if (isSuccess) {
					message = MessageConstant.MESSAGE_JOB_LINK_SEND_MSG.getMessage(response.getAcceptDate());
				} else {
					message = MessageConstant.MESSAGE_JOB_LINK_SEND_MSG_FAILURE.getMessage();
				}
			}
			execJobEndNode(runInstructionInfo, status, message, errorMessage, endValue);
			jtm.commit();
		} catch (Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * setMessage()で再送中情報を設定する
	 * 
	 * @param runInstructionInfo
	 *            実行指示
	 * @param errorMessage
	 *            エラーメッセージ
	 */
	public static void setFailureMessage(RunInstructionInfo runInstructionInfo, String errorMessage) {
		try {
			boolean isJobNestedEm = false;
			JpaTransactionManager jtm = new JpaTransactionManager();
			if (jtm.isNestedEm()) {
				// トランザクションが開始されている場合はトランザクション終了
				isJobNestedEm = true;
				try {
					m_log.debug("execJobEndNode() jtm.commit");
					// 外側のトランザクションをcommitする
					jtm.commit(true);
				} catch (Throwable e) {
					// ここは通らないはず
					m_log.error("execJobEndNode() jtm.commit ", e);
				} finally {
					if (jtm != null) {
						jtm.close();
					}
				}
			}
			// 対象ノード
			JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(
					runInstructionInfo.getSessionId(),
					runInstructionInfo.getJobunitId(),
					runInstructionInfo.getJobId(),
					runInstructionInfo.getFacilityId());
			// メッセージ送信
			new JobSessionNodeImpl().setMessage(sessionNode,
					"stdout=" + MessageConstant.MESSAGE_JOB_LINK_SEND_MSG_FAILURE.getMessage()
					+ ", stderr=" + errorMessage);
			if (isJobNestedEm) {
				// トランザクション開始
				jtm = new JpaTransactionManager();
				jtm.begin();
			}
		} catch (JobInfoNotFound e) {
			m_log.error("setNodeMessage() is error : " + ", SessionID=" + runInstructionInfo.getSessionId() + ", JobunitID="
					+ runInstructionInfo.getJobunitId() + ", JobID=" + runInstructionInfo.getJobId() + ", FacilityID="
					+ runInstructionInfo.getFacilityId());
		}
	}

	/**
	 * endNode()を実行する
	 * 
	 * @param runInstructionInfo
	 *            実行指示
	 * @param status
	 *            ステータス
	 * @param endValue
	 *            終了値
	 * @param message
	 *            メッセージ
	 * @param errorMessage
	 *            エラーメッセージ
	 * @return 処理結果
	 */
	private static boolean execJobEndNode(RunInstructionInfo runInstructionInfo, Integer status, String message,
			String errorMessage, Integer endValue) {

		boolean rtn = false;

		// メッセージ作成
		RunResultInfo resultInfo = new RunResultInfo();
		resultInfo.setSessionId(runInstructionInfo.getSessionId());
		resultInfo.setJobunitId(runInstructionInfo.getJobunitId());
		resultInfo.setJobId(runInstructionInfo.getJobId());
		resultInfo.setFacilityId(runInstructionInfo.getFacilityId());
		resultInfo.setCommand(runInstructionInfo.getCommand());
		resultInfo.setCommandType(runInstructionInfo.getCommandType());
		resultInfo.setStopType(runInstructionInfo.getStopType());
		resultInfo.setStatus(status);
		resultInfo.setMessage(message);
		resultInfo.setErrorMessage(errorMessage);
		resultInfo.setTime(HinemosTime.getDateInstance().getTime());
		resultInfo.setEndValue(endValue);
		try {
			boolean isJobNestedEm = false;
			JpaTransactionManager jtm = new JpaTransactionManager();
			if (jtm.isNestedEm()) {
				// トランザクションが開始されている場合はトランザクション終了
				isJobNestedEm = true;
				try {
					m_log.debug("execJobEndNode() jtm.commit");
					// 外側のトランザクションをcommitする
					jtm.commit(true);
				} catch (Throwable e) {
					// ここは通らないはず
					m_log.error("execJobEndNode() jtm.commit ", e);
				} finally {
					if (jtm != null) {
						jtm.close();
					}
				}
			}
			// メッセージ送信
			rtn = new JobRunManagementBean().endNode(resultInfo);
			if (isJobNestedEm) {
				// トランザクション開始
				jtm = new JpaTransactionManager();
				jtm.begin();
			}
		} catch (HinemosUnknown | JobInfoNotFound | InvalidRole e) {
			m_log.error("endNode() is error : " + ", SessionID=" + runInstructionInfo.getSessionId() + ", JobunitID="
					+ runInstructionInfo.getJobunitId() + ", JobID=" + runInstructionInfo.getJobId() + ", FacilityID="
					+ runInstructionInfo.getFacilityId());
		}
		return rtn;
	}

	/**
	 * 監視ジョブマップ削除
	 * 
	 * @param sessionId
	 *            セッションID
	 */
	public static void removeInfoBySessionId(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return;
		}

		/** RunHistory削除 */
		for (RunInstructionInfo runInstructionInfo : RunHistoryUtil.findRunHistoryBySessionId(sessionId)) {
			RunHistoryUtil.delRunHistory(runInstructionInfo);
		}
	}

	/**
	 * ジョブ連携送信ジョブを実行するスレッドクラス
	 */
	private static class JobLinkSendTask extends Thread {

		// Logger
		static private Log m_log = LogFactory.getLog(JobLinkSendTask.class);

		// 入力情報
		private RunInstructionInfo m_runInstructionInfo = null;

		/**
		 * コンストラクタ
		 * 
		 * @param runInstructionInfo
		 *            実行指示
		 */
		public JobLinkSendTask(RunInstructionInfo runInstructionInfo) {
			// 入力情報
			this.m_runInstructionInfo = runInstructionInfo;
		}

		/**
		 * ジョブ連携送信ジョブを実行するクラス<BR>
		 */
		@Override
		public void run() {

			m_log.info("run() SessionID=" + this.m_runInstructionInfo.getSessionId() + ", JobunitID="
					+ this.m_runInstructionInfo.getJobunitId() + ", JobID=" + this.m_runInstructionInfo.getJobId()
					+ ", FacilityID=" + this.m_runInstructionInfo.getFacilityId() + ", CommandType="
					+ this.m_runInstructionInfo.getCommandType());

			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				m_log.debug("JobMonitorTask: RunHistoryUtil.findRunHistory(m_runInstructionInfo) == "
						+ RunHistoryUtil.findRunHistory(m_runInstructionInfo));
				if (RunHistoryUtil.findRunHistory(m_runInstructionInfo) == null) {
					if (m_runInstructionInfo.getCommand().equals(CommandConstant.JOB_LINK_SEND)) {
						// メッセージ送信
						if (!execJobEndNode(m_runInstructionInfo, RunStatusConstant.START, "", "",
								MonitorJobConstant.INITIAL_END_VALUE_INFO)) {
							// ジョブがすでに起動している場合
							m_log.warn("This job already run by other agent. " + "SessionID="
									+ m_runInstructionInfo.getSessionId() + ", JobunitID="
									+ m_runInstructionInfo.getJobunitId() + ", JobID=" + m_runInstructionInfo.getJobId()
									+ ", FacilityID=" + m_runInstructionInfo.getFacilityId());
							return;
						}

						// 実行履歴に追加
						RunHistoryUtil.addRunHistory(m_runInstructionInfo);

						// 実行
						new JobRunManagementBean().runJobLinkSendJob(m_runInstructionInfo);

					} else {
						// ここは通らない
						m_log.warn("runJob() : command is not specified correctly.");
					}
				} else {
					// 処理を終了する。
					if (m_runInstructionInfo.getCommandType() == CommandTypeConstant.STOP
							&& m_runInstructionInfo.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {

						// 実行履歴が存在しない場合にはエラーを返す
						if (RunHistoryUtil.findRunHistory(m_runInstructionInfo) == null) {
							// メッセージ送信
							endJobLinkSendJob(m_runInstructionInfo, null,
									"Internal Error : Ex. Job already terminated.", RunStatusConstant.ERROR, false);
						} else {
							// 終了処理
							// キャンセル処理
							endJobLinkSendJob(m_runInstructionInfo, null, "", RunStatusConstant.END, true);
						}
					}
				}
				// 終了処理
				jtm.commit();

			} catch (Exception e) {
				// 実行時に失敗
				// メッセージ作成
				endJobLinkSendJob(m_runInstructionInfo, null, e.getMessage(), RunStatusConstant.ERROR, false);
				if (jtm != null)
					jtm.rollback();
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
	}

}
