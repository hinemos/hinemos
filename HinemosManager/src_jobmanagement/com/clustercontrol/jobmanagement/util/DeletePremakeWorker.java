/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.factory.JobSessionImpl;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.maintenance.factory.MaintenanceJob;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 事前生成ジョブセッションの削除を行うクラス<BR>
 */
public class DeletePremakeWorker {

	// Logger
	private static Log m_log = LogFactory.getLog(DeletePremakeWorker.class);

	private static ExecutorService service;
	private static String workerName = "DeletePremakeWorker";

	/** 検索タイムアウト **/
	private static Integer searchTimeout = 0;

	static {
		int maxThreadPoolSize = HinemosPropertyCommon.job_delete_premake_thread_pool_size.getIntegerValue();
		service = new MonitoredThreadPoolExecutor(maxThreadPoolSize, maxThreadPoolSize,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, workerName + "-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());

		searchTimeout = HinemosPropertyCommon.job_delete_premake_select_timeout.getIntegerValue();
	}

	/**
	 * 事前生成ジョブセッション削除処理
	 * 
	 * @param jobkickId 実行契機ID
	 */
	public static void deletePremake(String jobkickId) throws HinemosUnknown {

		if (jobkickId == null || jobkickId.isEmpty()) {
			throw new HinemosUnknown("deletePremake() Error : jobkickId is empty.");
		}

		m_log.info("deletePremake() start jobkickId=" + jobkickId);
		try {
			// タスクを実行する
			service.execute(new DeletePremakeTask(jobkickId));
		} catch(Throwable e) {
			throw new HinemosUnknown("deletePremake() error", e);
		}
	}

	/**
	 * 事前生成ジョブセッションの削除を行うスレッドクラス
	 */
	private static class DeletePremakeTask extends Thread {

		// Logger
		static private Log m_log = LogFactory.getLog(DeletePremakeTask.class);

		// 入力情報
		private String m_jobkickId = null;

		/**
		 * コンストラクタ
		 * 
		 * @param jobkickId 監視設定ID
		 */
		public DeletePremakeTask(String jobkickId) {
			m_jobkickId = jobkickId;
		}

		/**
		 * 事前生成ジョブセッションの削除を行うクラス<BR>
		 */
		@Override
		public void run() {

			// 処理件数
			int count = 0;

			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				HinemosEntityManager em = jtm.getEntityManager();

				synchronized (MaintenanceJob._deleteLock) {

					// 削除処理
					List<JobSessionEntity> list = QueryUtil.getJobSessionListByJobkickIdAndStatus(
							m_jobkickId, StatusConstant.TYPE_SCHEDULED, searchTimeout);
					for (JobSessionEntity entity : list) {
						ILockManager lm = LockManagerFactory.instance().create();
						lm.delete(JobSessionImpl.class.getName() + "-" + entity.getSessionId());
						em.remove(entity);
						count++;
					}
				}

				// 通知処理(処理終了)
				String[] args = {m_jobkickId, Integer.toString(count)};
				String orgMessage = MessageConstant.MESSAGE_JOBKICK_ORGMSG_DELETE_PREMAKE.getMessage(args);
				AplLogger.put(InternalIdCommon.JOB_SYS_022, new String[]{m_jobkickId}, orgMessage);

				// 終了処理
				jtm.commit();

			} catch (HinemosDbTimeout e) {
				// 通知処理(エラー)
				String[] args = {m_jobkickId, Integer.toString(count)};
				String orgMessage = MessageConstant.MESSAGE_JOBKICK_ORGMSG_DELETE_PREMAKE.getMessage(args);
				AplLogger.put(InternalIdCommon.JOB_SYS_023, new String[]{m_jobkickId}, orgMessage);
				if (jtm != null) {
					jtm.rollback();
				}
			} catch (Exception e) {
				// 処理失敗
				m_log.warn("run() error : jobkickId=" + this.m_jobkickId, e);
				// 通知処理(エラー)
				String[] args = {m_jobkickId, Integer.toString(count)};
				String orgMessage = MessageConstant.MESSAGE_JOBKICK_ORGMSG_DELETE_PREMAKE.getMessage(args);
				AplLogger.put(InternalIdCommon.JOB_SYS_023, new String[]{m_jobkickId}, orgMessage);
					if (jtm != null) {
					jtm.rollback();
				}
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}
	}
}
