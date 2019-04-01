/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.queue.JobQueue;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.queue.JobQueueItemNotFoundException;
import com.clustercontrol.jobmanagement.queue.JobQueueNotFoundException;
import com.clustercontrol.jobmanagement.util.JobSessionChangeDataCache;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Singletons;

/**
 * ジョブ操作の中断に関する処理を行うクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperateSuspendOfJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperateSuspendOfJob.class );

	/**
	 * コンストラクタ
	 */
	public OperateSuspendOfJob(){
		super();
	}

	/**
	 * ジョブを停止[中断]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown 
	 */
	public void suspendJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("suspendJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		int status = sessionJob.getStatus();

		// 実行中 -> 中断
		// 実行中(キュー待機) -> 中断(キュー待機)
		// 上記以外 -> return
		if (status == StatusConstant.TYPE_RUNNING) {
			sessionJob.setStatus(StatusConstant.TYPE_SUSPEND);
		} else if (status == StatusConstant.TYPE_RUNNING_QUEUE) {
			sessionJob.setStatus(StatusConstant.TYPE_SUSPEND_QUEUE);
			// ジョブキュー上でも中断状態にする
			String queueId = sessionJob.getJobInfoEntity().getQueueId(); // 状況的にqueueIdは!null
			try {
				JobQueue queue = Singletons.get(JobQueueContainer.class).get(queueId);
				queue.suspend(sessionId, jobunitId, jobId);
			} catch (JobQueueNotFoundException | JobQueueItemNotFoundException e) {
				// TYPE_*_QUEUEのジョブセッションが存在するにも関わらず
				// ジョブキュー、またはその中のジョブが存在しないことはありえない。
				throw new HinemosUnknown(e);
			}
		} else {
			return;
		}

		// 承認ジョブの操作
		if (sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB) {
			// セッションジョブに関連するセッションノードを取得
			List<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
			JobSessionNodeEntity sessionNode = null;

			// 承認ジョブの場合はノードリストは1件のみ
			if (nodeList != null && nodeList.size() == 1) {
				// セッションノードを取得
				sessionNode = nodeList.get(0);
			} else {
				m_log.error("approveJob() not found job info:" + sessionJob.getId().getJobId());
				throw new JobInfoNotFound();
			}
			sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_SUSPEND);
		}

		// 終了・中断日時を設定
		sessionJob.setEndDate(HinemosTime.currentTimeMillis());
		// ジョブ履歴用キャッシュ更新
		JobSessionChangeDataCache.add(sessionJob);
		// 収集データ更新
		CollectDataUtil.put(sessionJob);

		// 配下のジョブ（実行中 or キュー待機）に対しても、中断を伝播する。
		List<JobSessionJobEntity> children = QueryUtil.getJobSessionJobByParentStatus(sessionId, jobunitId, jobId,
				StatusConstant.TYPE_RUNNING);
		children.addAll(QueryUtil.getJobSessionJobByParentStatus(sessionId, jobunitId, jobId,
				StatusConstant.TYPE_RUNNING_QUEUE));

		for (JobSessionJobEntity child : children) {
			suspendJob(child.getId().getSessionId(), child.getId().getJobunitId(), child.getId().getJobId());
		}
	}

	/**
	 * ジョブを開始[中断解除]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public void releaseSuspendJob(String sessionId, String jobunitId, String jobId)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("releaseSuspendJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//ジョブ中断解除処理
		releaseSuspend(sessionId, jobunitId, jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		// 配下のジョブに対しても、中断解除を伝播する。
		List<JobSessionJobEntity> children = QueryUtil.getJobSessionJobByParentStatus(sessionId, jobunitId, jobId,
				StatusConstant.TYPE_SUSPEND);
		children.addAll(QueryUtil.getJobSessionJobByParentStatus(sessionId, jobunitId, jobId,
				StatusConstant.TYPE_SUSPEND_QUEUE));

		for (JobSessionJobEntity child : children) {
			releaseSuspendJob(sessionId, child.getId().getJobunitId(), child.getId().getJobId());
		}

		// ノードへの実行指示(終了していたらendJobを実行)
		// ジョブを中断して、ノード詳細が終了した後に、ジョブの中断解除をした場合に
		// ジョブが RUNNINGのままで止まってしまうため、この処理が必要。
		if (sessionJob.hasSessionNode()) {
			if (new JobSessionNodeImpl().startNode(sessionId, jobunitId, jobId)) {
				new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, null, false);
			}
		}
	}

	/**
	 * ジョブ中断解除処理を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown 
	 */
	private void releaseSuspend(String sessionId, String jobunitId, String jobId)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("releaseSuspend() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		int status = sessionJob.getStatus();

		// 中断 -> 実行中
		// 中断(キュー待機) -> 実行中(キュー待機)
		// 上記以外 -> return
		if (status == StatusConstant.TYPE_SUSPEND) {
			sessionJob.setStatus(StatusConstant.TYPE_RUNNING);
		} else if (status == StatusConstant.TYPE_SUSPEND_QUEUE) {
			sessionJob.setStatus(StatusConstant.TYPE_RUNNING_QUEUE);
			// ジョブキュー上でも中断解除する
			String queueId = sessionJob.getJobInfoEntity().getQueueId(); // 状況的にqueueIdは!null
			try {
				JobQueue queue = Singletons.get(JobQueueContainer.class).get(queueId);
				queue.resume(sessionId, jobunitId, jobId);
			} catch (JobQueueNotFoundException | JobQueueItemNotFoundException e) {
				// TYPE_*_QUEUEのジョブセッションが存在するにも関わらず
				// ジョブキュー、またはその中のジョブが存在しないことはありえない。
				throw new HinemosUnknown(e);
			}
		} else {
			return;
		}

		if (sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB) {
			// セッションジョブに関連するセッションノードを取得
			List<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
			JobSessionNodeEntity sessionNode = null;

			// 承認ジョブの場合はノードリストは1件のみ
			if (nodeList != null && nodeList.size() == 1) {
				// セッションノードを取得
				sessionNode = nodeList.get(0);
			} else {
				m_log.error("approveJob() not found job info:" + sessionJob.getId().getJobId());
				throw new JobInfoNotFound();
			}
			if (sessionNode.getApprovalResult() == null) {
				sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_PENDING);
			}
		}

		// 親ジョブも中断解除する
		String parentJobUnitId = sessionJob.getParentJobunitId();
		String parentJobId = sessionJob.getParentJobId();
		if (!CreateJobSession.TOP_JOB_ID.equals(parentJobId)) {
			releaseSuspend(sessionId, parentJobUnitId, parentJobId);
		}
	}
}
