/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.Collection;
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
import com.clustercontrol.jobmanagement.util.JobSessionChangeDataCache;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

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
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 */
	public void suspendJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {

		m_log.debug("suspendJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		if(sessionJob != null){
			//実行状態が実行中の場合、実行状態を中断にする
			if(sessionJob.getStatus() == StatusConstant.TYPE_RUNNING){
				sessionJob.setStatus(StatusConstant.TYPE_SUSPEND);
				
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB){
					//セッションジョブに関連するセッションノードを取得
					List<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
					JobSessionNodeEntity sessionNode =null;
					
					// 承認ジョブの場合はノードリストは1件のみ
					if(nodeList != null && nodeList.size() == 1){
						//セッションノードを取得
						sessionNode =nodeList.get(0);
					}else{
						m_log.error("approveJob() not found job info:" + sessionJob.getId().getJobId());
						throw new JobInfoNotFound();
					}
					sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_SUSPEND);
				}
				
				//終了・中断日時を設定
				sessionJob.setEndDate(HinemosTime.currentTimeMillis());
				// ジョブ履歴用キャッシュ更新
				JobSessionChangeDataCache.add(sessionJob);
				// 収集データ更新
				CollectDataUtil.put(sessionJob);

				//セッションIDとジョブIDから、直下のジョブを取得（実行状態が実行中）
				Collection<JobSessionJobEntity> collection
					= QueryUtil.getJobSessionJobByParentStatus(
							sessionId, jobunitId, jobId, StatusConstant.TYPE_RUNNING);
				if (collection == null) {
					JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByParentStatus"
							+ ", [sessionId, parentJobId, status] = "
							+ "[" + sessionId + ", " + jobId+ ", " + StatusConstant.TYPE_RUNNING + "]");
					m_log.info("suspendJob() : "
							+ je.getClass().getSimpleName() + ", " + je.getMessage());
					je.setSessionId(sessionId);
					je.setParentJobId(jobId);
					je.setStatus(StatusConstant.TYPE_RUNNING);
					throw je;
				}

				for (JobSessionJobEntity child : collection) {

					//ジョブ中断処理を行う
					suspendJob(child.getId().getSessionId(), child.getId().getJobunitId(), child.getId().getJobId());
				}
			}
		}
	}

	/**
	 * ジョブを開始[中断解除]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 *
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#checkJobEnd(JobSessionJobLocal)
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#checkEndStatus(String, String, String)
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#setEndStatus(String, String, String, Integer, Integer, String)
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#endJob(String, String, String)
	 * @see com.clustercontrol.jobmanagement.factory.OperateSuspendOfJob#releaseSuspend(String, String)
	 * @see com.clustercontrol.jobmanagement.factory.Notice#notify(String, String, Integer)
	 */
	public void releaseSuspendJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("releaseSuspendJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//ジョブ中断解除処理
		releaseSuspend(sessionId, jobunitId, jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		//セッションIDとジョブIDから、直下のジョブを取得（実行状態が中断）
		Collection<JobSessionJobEntity> collection
		= QueryUtil.getJobSessionJobByParentStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_SUSPEND);
		if (collection == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByParentStatus"
					+ ", [sessionId, parentJobId, status] = "
					+ "[" + sessionId + ", " + jobId + ", " + StatusConstant.TYPE_SUSPEND + "]");
			m_log.info("releaseSuspendJob() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			je.setSessionId(sessionId);
			je.setParentJobId(jobId);
			je.setStatus(StatusConstant.TYPE_SUSPEND);
			throw je;
		}
		for (JobSessionJobEntity child : collection) {
			//ジョブ中断解除処理を行う
			releaseSuspendJob(sessionId, child.getId().getJobunitId(), child.getId().getJobId());
		}

		if(sessionJob != null){
			//ノードへの実行指示(終了していたらendJobを実行)
			// ここはジョブを中断にして、ノード詳細で終了した後に、ジョブの中断解除をしたら、
			// RUNNINGのままで止まってしまう。
			// それを回避するために下記の実装を加える。
			if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB) {
				if (new JobSessionNodeImpl().startNode(sessionId, jobunitId, jobId)) {
					new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, null, false);
				}
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
	 */
	private void releaseSuspend(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("releaseSuspend() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		//実行状態が中断の場合
		if(sessionJob.getStatus() == StatusConstant.TYPE_SUSPEND){
			//実行状態を実行中にする
			sessionJob.setStatus(StatusConstant.TYPE_RUNNING);
			
			if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB){
				//セッションジョブに関連するセッションノードを取得
				List<JobSessionNodeEntity> nodeList = sessionJob.getJobSessionNodeEntities();
				JobSessionNodeEntity sessionNode =null;
				
				// 承認ジョブの場合はノードリストは1件のみ
				if(nodeList != null && nodeList.size() == 1){
					//セッションノードを取得
					sessionNode =nodeList.get(0);
				}else{
					m_log.error("approveJob() not found job info:" + sessionJob.getId().getJobId());
					throw new JobInfoNotFound();
				}
				if(sessionNode.getApprovalResult() == null){
					sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_PENDING);
				}
			}

			//リレーションを取得し、親ジョブのジョブIDを取得
			String parentJobUnitId = sessionJob.getParentJobunitId();
			String parentJobId = sessionJob.getParentJobId();

			if(!CreateJobSession.TOP_JOB_ID.equals(parentJobId)){
				releaseSuspend(sessionId, parentJobUnitId, parentJobId);
			}
		}
	}
}
