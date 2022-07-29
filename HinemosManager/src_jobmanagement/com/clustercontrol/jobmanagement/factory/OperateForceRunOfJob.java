/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.queue.JobQueueNotFoundException;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.Singletons;

/**
 * ジョブ操作の開始[強制実行]を行うクラスです。
 *
 * @since 6.2.0
 */
public class OperateForceRunOfJob {
	/** ログ出力のインスタンス */
	private static Log log = LogFactory.getLog(OperateForceRunOfJob.class);

	/**
	 * ジョブを開始[強制実行]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @throws InvalidRole
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public void forceRunJob(String sessionId, String jobunitId, String jobId)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		log.debug("forceRunJob: sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		// 「中断(キュー待機)」状態なら、いったん「実行中(キュー待機)」へ遷移させる
		if (sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND_QUEUE) {
			log.debug("forceRunJob: SUSPEND_QUEUE -> RUNNING_QUEUE");
			new OperateSuspendOfJob().releaseSuspendJob(sessionId, jobunitId, jobId);
		}

		// 「実行中(キュー待機)」から「実行中」へ遷移させる
		if (sessionJob.getStatus() == StatusConstant.TYPE_RUNNING_QUEUE) {
			log.debug("forceRunJob: RUNNING_QUEUE -> RUNNING");
			String queueId = sessionJob.getJobInfoEntity().getQueueId(); // 状況的にqueueIdは!null
			try {
				Singletons.get(JobQueueContainer.class).get(queueId).forceActivateJob(sessionId, jobunitId, jobId);
			} catch (JobQueueNotFoundException e) {
				// 論理エラーしか考えられない。
				throw new HinemosUnknown(e);
			}
		}
	}
}
