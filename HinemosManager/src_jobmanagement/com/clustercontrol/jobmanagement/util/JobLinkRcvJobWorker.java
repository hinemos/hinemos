/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageInfo;
import com.clustercontrol.jobmanagement.bean.QuartzConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.model.JobLinkMessageEntity;
import com.clustercontrol.jobmanagement.model.JobLinkMessageExpInfoEntity;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * HinemosManager上でジョブ連携送信ジョブを実行するクラス<BR>
 */
public class JobLinkRcvJobWorker {

	// Logger
	private static Log m_log = LogFactory.getLog(JobLinkRcvJobWorker.class);

	private static ExecutorService service;
	private static String workerName = "JobLinkRcvJobWorker";
	private final static int SCHEDULER_NAME_LENGTH = 256;

	static {
		int maxThreadPoolSize = HinemosPropertyCommon.joblink_rcv_job_thread_pool_size.getIntegerValue();

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
	 * スケジューラ制御用ロックキー取得
	 * 
	 * @param key
	 * @return スケジューラ制御用ロックキー
	 */
	private static String getSchedulerKey(String key) {
		return String.format("%s[%s]", JobLinkRcvJobWorker.class.getName(), key);
	}

	/**
	 * スケジューラ制御用ロック作成
	 * @param key
	 * @return スケジューラ制御用ロック
	 */
	private static ILock addSchedulerLock(String key) {
		m_log.debug("addSchedulerLock() start : key=" + key);
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(getSchedulerKey(key));
		return lock;
	}

	/**
	 * スケジューラ制御用ロック取得
	 * @param key
	 * @return スケジューラ制御用ロック
	 */
	public static ILock getSchedulerLock(String key) {
		m_log.debug("getSchedulerLock() start : key=" + key);
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.get(getSchedulerKey(key));
		return lock;
	}

	/**
	 * スケジューラ制御用ロック削除
	 * @param key
	 * @return true:削除成功、false:削除失敗
	 */
	public static boolean deleteSchedulerLock(String key) {
		m_log.debug("deleteSchedulerLock() start : key=" + key);
		ILockManager lockManager = LockManagerFactory.instance().create();
		boolean rtn = lockManager.delete(getSchedulerKey(key));
		if (!rtn) {
			m_log.warn("deleteSchedulerLock() lock is not found. : key=" + key);
		}
		return rtn;
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
			service.execute(new JobLinkRcvTask(runInstructionInfo));
		} catch (Throwable e) {
			m_log.warn("runJob() Error : " + e.getMessage());
		}
	}

	/**
	 * ジョブ連携待機ジョブを終了する。
	 * 
	 * @param runInstructionInfo
	 *            実行指示
	 * @param errorMessage
	 *            エラーメッセージ
	 * @param status
	 *            ステータス
	 * @param returnValue
	 *            セッションノード戻り値
	 * @param messageEntity
	 *            メッセージ情報
	 * @param isScheduleJob
	 *            true:スケジューラで実行されている
	 */
	public static void endJobLinkRcvJob(RunInstructionInfo runInstructionInfo, String errorMessage, Integer status,
			Integer endValue, JobLinkMessageEntity jobLinkMessageEntity, boolean isScheduleJob) {

		m_log.debug("endJobLinkRcvJob() : sessionId=" + runInstructionInfo.getSessionId() + ", JobunitId="
				+ runInstructionInfo.getJobunitId() + ", JobId=" + runInstructionInfo.getJobId());

		ILock lock = null;
		String key = getKey(runInstructionInfo);

		if (!isScheduleJob) {
			lock = getSchedulerLock(key);
			if (lock == null) {
				return;
			}
		}
		JpaTransactionManager jtm = null;
		try {
			if (lock != null) {
				lock.writeLock();
				if (getSchedulerLock(key) == null) {
					return;
				}
			}
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 履歴削除
			RunHistoryUtil.delRunHistory(runInstructionInfo);

			// メッセージ送信
			execJobEndNode(runInstructionInfo, status, errorMessage, endValue, jobLinkMessageEntity, true, null, isScheduleJob);

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
			if (lock != null) {
				lock.writeUnlock();
				deleteSchedulerLock(key);
			}
		}
	}

	/**
	 * endNode()を実行する
	 * 
	 * @param runInstructionInfo
	 *            実行指示
	 * @param status
	 *            ステータス
	 * @param errorMessage
	 *            エラーメッセージ
	 * @param returnValue
	 *            セッションノード戻り値
	 * @param isDeleteSchedule
	 *            true:スケジュールを削除する
	 * @param startTime
	 *            開始日時(ジョブ開始時以外はnull)
	 * @param isScheduleJob
	 *            true:スケジューラで実行されている
	 * @return 処理結果
	 */
	private static boolean execJobEndNode(RunInstructionInfo runInstructionInfo, Integer status, String errorMessage,
			Integer endValue, JobLinkMessageEntity jobLinkMessageEntity, boolean isDeleteSchedule, Long startTime, boolean isScheduleJob) {

		boolean rtn = false;

		// メッセージ作成
		String message = "";
		if (jobLinkMessageEntity != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			String[] args = { jobLinkMessageEntity.getId().getJoblinkMessageId(),
					jobLinkMessageEntity.getId().getFacilityId(),
					sdf.format(jobLinkMessageEntity.getId().getSendDate()) };
			message = MessageConstant.MESSAGE_JOB_LINK_RCV_MSG.getMessage(args);
		}
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
		if (startTime != null) {
			resultInfo.setTime(startTime);
		} else {
			resultInfo.setTime(HinemosTime.getDateInstance().getTime());
		}
		resultInfo.setEndValue(endValue);
		if (jobLinkMessageEntity != null) {
			JobLinkMessageInfo jobLinkMessageInfo = new JobLinkMessageInfo();
			jobLinkMessageInfo.setJoblinkMessageId(jobLinkMessageEntity.getId().getJoblinkMessageId());
			jobLinkMessageInfo.setFacilityId(jobLinkMessageEntity.getId().getFacilityId());
			jobLinkMessageInfo.setSendDate(jobLinkMessageEntity.getId().getSendDate());
			jobLinkMessageInfo.setAcceptDate(jobLinkMessageEntity.getAcceptDate());
			jobLinkMessageInfo.setFacilityName(jobLinkMessageEntity.getFacilityName());
			jobLinkMessageInfo.setIpAddress(jobLinkMessageEntity.getIpAddress());
			jobLinkMessageInfo.setMonitorDetailId(jobLinkMessageEntity.getMonitorDetailId());
			jobLinkMessageInfo.setApplication(jobLinkMessageEntity.getApplication());
			jobLinkMessageInfo.setPriority(jobLinkMessageEntity.getPriority());
			jobLinkMessageInfo.setMessage(jobLinkMessageEntity.getMessage());
			jobLinkMessageInfo.setMessageOrg(jobLinkMessageEntity.getMessageOrg());
			if (jobLinkMessageEntity.getJobLinkMessageExpInfoEntities() != null) {
				jobLinkMessageInfo.setJobLinkExpInfo(new ArrayList<>());
				for (JobLinkMessageExpInfoEntity expEntity : jobLinkMessageEntity.getJobLinkMessageExpInfoEntities()) {
					JobLinkExpInfo expInfo = new JobLinkExpInfo();
					expInfo.setKey(expEntity.getId().getKey());
					expInfo.setValue(expEntity.getValue());
					jobLinkMessageInfo.getJobLinkExpInfo().add(expInfo);
				}
			}
			resultInfo.setJobLinkMessageInfo(jobLinkMessageInfo);
		}
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

		if (isDeleteSchedule) {
			try {
				// スケジュール削除
				deleteSchedule(runInstructionInfo, isScheduleJob);
			} catch (HinemosUnknown e) {
				// エラーとしない
				m_log.debug("schedule is not found.");
			}
		}

		return rtn;
	}

	/**
	 * ジョブ連携待機ジョブ情報をスケジューラに登録する
	 * 
	 * @param runInstructionInfo
	 *            指示情報
	 * @param startTime
	 *            開始日時
	 * @throws HinemosUnknown
	 */
	private static void updateSchedule(RunInstructionInfo runInstructionInfo, Long startTime) throws HinemosUnknown {

		// nullチェック
		if (runInstructionInfo == null) {
			HinemosUnknown e = new HinemosUnknown("runInstructionInfo is null.");
			m_log.warn("updateSchedule() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		// キー情報
		String key = getKey(runInstructionInfo);

		// スケジューラ用Lock作成
		ILock lock = addSchedulerLock(key);

		try {
			lock.writeLock();

			// JobDetailに呼び出すメソッドの引数を設定
			Serializable[] jdArgs = new Serializable[2];
			@SuppressWarnings("unchecked")
			Class<? extends Serializable>[] jdArgsType = new Class[2];
			// 引数：キー
			jdArgsType[0] = String.class;
			jdArgs[0] = key;
			// 引数：開始日時
			jdArgsType[1] = Long.class;
			jdArgs[1] = startTime;

			String name = "";
			if (key.length() > SCHEDULER_NAME_LENGTH) {
				name = key.substring(0, SCHEDULER_NAME_LENGTH);
			} else {
				name = key;
			}
			// SimpleTrigger でジョブをスケジューリング登録
			SchedulerPlugin.scheduleSimpleJob(SchedulerType.DBMS_JOB, name,
					QuartzConstant.GROUP_NAME_FOR_JOBLINKRCVJOB, HinemosTime.currentTimeMillis(),
					RunInterval.TYPE_MIN_01.toSec(), true, JobRunManagementBean.class.getName(),
					QuartzConstant.METHOD_NAME_FOR_JOBLINKRCVJOB, jdArgsType, jdArgs);
		} finally {
			lock.writeUnlock();
		}
	}

	/**
	 * ジョブ連携待機ジョブ情報をスケジューラから削除
	 * 
	 * @param runInstructionInfo
	 *            指示情報
	 * @param isScheduleJob true:スケジューラで実行されている
	 * @throws HinemosUnknown
	 */
	private static void deleteSchedule(RunInstructionInfo runInstructionInfo, boolean isScheduleJob) throws HinemosUnknown {
		m_log.debug("deleteSchedule() start");

		// nullチェック
		if (runInstructionInfo == null) {
			HinemosUnknown e = new HinemosUnknown("runInstructionInfo is null.");
			m_log.warn("deleteSchedule() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		String key = getKey(runInstructionInfo);
		String name;
		if (key.length() > SCHEDULER_NAME_LENGTH) {
			name = key.substring(0, SCHEDULER_NAME_LENGTH);
		} else {
			name = key;
		}
		if (isScheduleJob) {
			// まさにスケジューラで動いている自分自身を削除するのでスレッドがinterrupt状態になってしまう
			// Thread.interrupted()で消費してもよいがそもそも削除を最後におこなうようにpostCloseで削除する
			// このコールバックが実行されると、同一スレッドでThread.sleep()等実行時にInterruptExceptionが発生するので注意
			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				jtm.addCallback(new EmptyJpaTransactionCallback() {
					@Override
					public void postClose() {
						try {
							m_log.debug("deleteSchedule() : delete schedule start : key=" + key);
							SchedulerPlugin.deleteJob(SchedulerType.DBMS_JOB, name,
									QuartzConstant.GROUP_NAME_FOR_JOBLINKRCVJOB);
							m_log.debug("deleteSchedule() : delete schedule end : key=" + key);
						} catch (Exception e) {
							m_log.error(e.getMessage(), e);
						}
					}
				});
			}
		} else {
			// 即削除する
			try {
				m_log.debug("deleteSchedule() : delete schedule start : key=" + key);
				SchedulerPlugin.deleteJob(SchedulerType.DBMS_JOB, name,
						QuartzConstant.GROUP_NAME_FOR_JOBLINKRCVJOB);
			} catch (Exception e) {
				m_log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * ジョブ連携待機ジョブ情報削除
	 * 履歴削除から呼び出されるため、スケジューラ制御用のロックは取得しない。
	 * そのため、削除対象のジョブ連携待機ジョブが実行中の場合は、EclipselinkでConcurrencyExceptionが
	 * 発生する可能性がある。
	 * 
	 * @param sessionId
	 *            セッションID
	 */
	public static void removeInfoBySessionId(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return;
		}

		List<RunInstructionInfo> runInstructionInfoList = RunHistoryUtil.findRunHistoryBySessionId(sessionId);
		for (RunInstructionInfo runInstructionInfo : runInstructionInfoList) {
			try {
				/** スケジュール削除 */
				deleteSchedule(runInstructionInfo, false);
				/** RunHistory削除 */
				RunHistoryUtil.delRunHistory(runInstructionInfo);
				/** スケジューラ制御用ロック削除 */
				JobLinkRcvJobWorker.deleteSchedulerLock(getKey(runInstructionInfo));
			} catch (HinemosUnknown e) {
				// エラーとしない
				m_log.debug("schedule is not found.");
			}
		}
	}

	/**
	 * 指示情報を元にキー文字列を返す
	 * 
	 * @param runInstructionInfo
	 *            指示情報
	 * @return キー文字列
	 */
	private static String getKey(RunInstructionInfo runInstructionInfo) {
		return runInstructionInfo.getSessionId() + "#" + runInstructionInfo.getJobunitId() + "#"
				+ runInstructionInfo.getJobId() + "#" + runInstructionInfo.getFacilityId();
	}

	/**
	 * キー文字列を基にスケジューラロック、指示情報を登録する
	 * 
	 * @param キー文字列
	 */
	public static void createInfo(String key) {

		m_log.debug("createInfo() start : key=" + key);

		// ジョブ連携待機ジョブのスケジューラロック登録
		addSchedulerLock(key);

		// 実行履歴に追加
		RunHistoryUtil.addRunHistory(getRunInstructionInfo(key));
	}

	/**
	 * 実行指示情報取得
	 * 
	 * @param key キー文字列
	 * @return 指示情報
	 */
	public static RunInstructionInfo getRunInstructionInfo(String key) {
		Pattern pattern = Pattern.compile("([^:]*)#([^:]*)#([^:]*)#([^:]*)");
		Matcher matcher = pattern.matcher(key);
		if (!matcher.find()) {
			m_log.warn("addRunInstructionInfo : incorrect key. key=" + key);
			return new RunInstructionInfo();
		}
		//実行指示情報を作成
		RunInstructionInfo instructionInfo = new RunInstructionInfo();
		instructionInfo.setSessionId(matcher.group(1));
		instructionInfo.setJobunitId(matcher.group(2));
		instructionInfo.setJobId(matcher.group(3));
		instructionInfo.setFacilityId(matcher.group(4));
		instructionInfo.setCommand(CommandConstant.JOB_LINK_RCV);
		instructionInfo.setCommandType(CommandTypeConstant.NORMAL);
		return instructionInfo;
	}

	/**
	 * ジョブ連携待機ジョブを実行するスレッドクラス
	 */
	private static class JobLinkRcvTask extends Thread {

		// Logger
		static private Log m_log = LogFactory.getLog(JobLinkRcvTask.class);

		// 入力情報
		private RunInstructionInfo m_runInstructionInfo = null;

		/**
		 * コンストラクタ
		 * 
		 * @param runInstructionInfo
		 *            実行指示
		 */
		public JobLinkRcvTask(RunInstructionInfo runInstructionInfo) {
			// 入力情報
			this.m_runInstructionInfo = runInstructionInfo;
		}

		/**
		 * ジョブ連携待機ジョブを実行するクラス<BR>
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
					if (m_runInstructionInfo.getCommand().equals(CommandConstant.JOB_LINK_RCV)) {
						Long startTime = HinemosTime.getDateInstance().getTime();
						// メッセージ送信
						if (!execJobEndNode(m_runInstructionInfo, RunStatusConstant.START, "",
								JobLinkConstant.RCV_INITIAL_END_VALUE_INFO, null, false, startTime, false)) {
							// ジョブがすでに起動している場合
							m_log.warn("This job already run by other agent. " + "SessionID="
									+ m_runInstructionInfo.getSessionId() + ", JobunitID="
									+ m_runInstructionInfo.getJobunitId() + ", JobID=" + m_runInstructionInfo.getJobId()
									+ ", FacilityID=" + m_runInstructionInfo.getFacilityId());
							return;
						}

						// 実行履歴に追加
						RunHistoryUtil.addRunHistory(m_runInstructionInfo);

						// スケジュールに登録
						updateSchedule(m_runInstructionInfo, startTime);

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
							endJobLinkRcvJob(m_runInstructionInfo, "Internal Error : Ex. Job already terminated.",
									RunStatusConstant.ERROR, JobLinkConstant.RCV_INITIAL_END_VALUE_TIMEOUT, null, false);
						} else {
							// 終了処理
							// キャンセル処理
							endJobLinkRcvJob(m_runInstructionInfo, "", RunStatusConstant.END,
									JobLinkConstant.RCV_INITIAL_END_VALUE_INFO, null, false);
						}
					}
				}
				// 終了処理
				jtm.commit();

			} catch (Exception e) {
				// 実行時に失敗
				// メッセージ作成
				endJobLinkRcvJob(m_runInstructionInfo, e.getMessage(), RunStatusConstant.ERROR,
						JobLinkConstant.RCV_INITIAL_END_VALUE_TIMEOUT, null, false);
				if (jtm != null)
					jtm.rollback();
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
	}

}
