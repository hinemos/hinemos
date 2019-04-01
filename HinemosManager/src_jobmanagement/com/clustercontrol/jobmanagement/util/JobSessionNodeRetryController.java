/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import java.text.SimpleDateFormat;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.util.HinemosTime;

/**
 * ノード詳細のリトライを行う際の、遅延実行を管理する。
 */
public class JobSessionNodeRetryController {

	// Logger
	private static Log m_log = LogFactory.getLog(JobSessionNodeRetryController.class);

	// 遅延実行待機中のノード詳細情報を蓄積するキュー
	private static final Queue<JobRetryInfo> queue = new ConcurrentLinkedQueue<>();

	// リトライを処理するスレッドの数
	private static final int THREAD_POOL_SIZE = 1;

	// 遅延実行を行うexecutor
	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE,
			new RetryThreadFactory());

	// 毎回newする無駄を省くため
	private static final JobSessionNodeImpl jobSessionNodeImpl = new JobSessionNodeImpl();

	/**
	 * 再実行を登録する。
	 * 
	 * @param sessionNodePK
	 *            再実行するジョブ詳細。
	 */
	public static void register(JobSessionNodeEntityPK sessionNodePK) {
		JobRetryInfo jobRetryInfo = new JobRetryInfo(sessionNodePK);

		// 既にリトライ待機中なら何もしない
		synchronized (queue) {
			if (queue.contains(jobRetryInfo)) {
				return;
			}
			queue.add(jobRetryInfo);
			m_log.info("register() : Added. " + sessionNodePK);
		}

		// Hinemosプロパティで指定された遅延ののち、RetryRunnerを実行する
		int jobRetryInterval = HinemosPropertyCommon.job_retry_interval.getIntegerValue();
		executor.schedule(new RetryRunner(jobRetryInfo), jobRetryInterval, TimeUnit.MILLISECONDS);
	}

	/**
	 * 指定されたノード詳細が、リトライ待ちとして登録中かどうかを返す。
	 * 
	 * @param sessionNodePK
	 *            ノード詳細のID。
	 * @return true:登録中、false:登録中ではない(待機が完了して開始判定を行っている状態も含む)。
	 */
	public static boolean isRegistered(JobSessionNodeEntityPK sessionNodePK) {
		synchronized (queue) {
			for (JobRetryInfo info : queue) {
				if (info.getSessionNodePK().equals(sessionNodePK)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 待機中タスクの情報(レポート)を、文字列で返す。
	 */
	public static String getReport() {
		StringBuilder report = new StringBuilder();
		report.append("Retry:\n");
		// 改行コードについて、System.lineSeparator()を使わず、
		// JobMultiplicityCache.getJobQueueStr()が"\n"を指定しているのに合わせてある。
		for (JobRetryInfo info : queue) {
			report.append(info.toString());
			report.append("\n");
		}
		return report.toString();
	}

	// キューに持つ情報
	private static class JobRetryInfo {
		private JobSessionNodeEntityPK sessionNodePK;
		private long queuedTime;

		public JobRetryInfo(JobSessionNodeEntityPK sessionNodePK) {
			this.sessionNodePK = sessionNodePK;
			this.queuedTime = HinemosTime.currentTimeMillis();
		}

		public JobSessionNodeEntityPK getSessionNodePK() {
			return sessionNodePK;
		}

		@Override
		public String toString() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			return "[" + sessionNodePK.getSessionId() + "," + sessionNodePK.getJobunitId() + ","
					+ sessionNodePK.getJobId() + "," + sessionNodePK.getFacilityId() + "] queued at "
					+ sdf.format(queuedTime);
		}
	}

	// RetryRunnerを実行するスレッドを生成するクラス
	private static class RetryThreadFactory implements ThreadFactory {
		private static final AtomicInteger serial = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "JobRetry-" + serial.incrementAndGet());
		}
	}

	// ジョブの再実行を行うクラス
	private static class RetryRunner implements Runnable {

		private final JobRetryInfo jobRetryInfo;

		public RetryRunner(JobRetryInfo jobRetryInfo) {
			this.jobRetryInfo = jobRetryInfo;
		}

		@Override
		public void run() {
			try {
				run0();
			} catch (Exception e) {
				// run()外部に例外が伝播しないようにする。
				m_log.error("run() : Exception occuered. " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}

		private void run0() {
			JobSessionNodeEntityPK sessionNodePK = jobRetryInfo.getSessionNodePK();
			ILock sessionLock = JobRunManagementBean.getLock(sessionNodePK.getSessionId());
			try {
				sessionLock.writeLock();
				JpaTransactionManager jtm = new JpaTransactionManager();
				try {
					jtm.begin(true);

					JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionNodePK.getSessionId(),
							sessionNodePK.getJobunitId(), sessionNodePK.getJobId(), sessionNodePK.getFacilityId());
					boolean result = jobSessionNodeImpl.startNodeSub(sessionNode);

					// リトライは比較的生じにくいケースであるため、infoでログを記録しても問題ないはず。
					if (result) {
						m_log.info("run() : Start the node. " + sessionNodePK);
					} else {
						m_log.info("run() : Cannot start the node. " + sessionNodePK);
					}

					jtm.commit();
				} catch (Exception e) {
					jtm.rollback();
					m_log.error("run() : Retry failed. " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				} finally {
					jtm.close();
				}

				// 例外の発生有無に関わらず、キューからは削除する。
				// 例外が発生した場合は、毎分0秒の待機ジョブチェックに実行開始を委ねることになる。
				queue.remove(jobRetryInfo);
			} finally {
				sessionLock.writeUnlock();
			}
		}
	}

}
