/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.factory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.EndStatusCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.util.FromRunningAfterCommitCallback;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * ジョブ操作の開始[即時]を行うクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperateStartOfJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperateStartOfJob.class );

	/**
	 * コンストラクタ
	 */
	public OperateStartOfJob(){
		super();
	}

	/**
	 * ジョブを開始[即時]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public void startNode(String sessionId, String jobunitId, String jobId, String facilityId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {

		m_log.debug("startJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);

		startJobPre(sessionId, jobunitId, jobId, facilityId);

		//ジョブネット、ジョブの場合、ジョブ開始処理を行う
		new JobSessionJobImpl().startJob(sessionId, jobunitId, jobId);
	}

	/**
	 * ジョブを開始[即時]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public void startJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("startJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		startJobPre(sessionId, jobunitId, jobId, null);

		//ジョブ開始処理を行う
		new JobSessionJobImpl().startJob(sessionId, jobunitId, jobId);
	}

	private JobSessionJobEntity startJobPre(String sessionId, String jobunitId, String jobId, String facilityId)
			throws JobInfoNotFound, InvalidRole {
		//実行状態クリア
		clearStatus(sessionId, jobunitId, jobId, facilityId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		//親ジョブの実行状態を実行中に遷移させる(自分自身の実行状態は JobSessionJobImpl().startJobで実施
		setStatus(sessionId, sessionJob.getParentJobunitId(), sessionJob.getParentJobId(), StatusConstant.TYPE_RUNNING);

		return sessionJob;
	}

	/**
	 * ジョブの実行結果をクリアします。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#clearJob(String, String)
	 */
	private void clearStatus(String sessionId, String jobunitId, String jobId, String facilityId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("clearStatus() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		JobInfoEntity job = sessionJob.getJobInfoEntity();

		//終了状態、終了値、終了・中断日時、開始・再実行日時をクリア
		sessionJob.setEndStatus(null);
		sessionJob.setEndValue(null);
		sessionJob.setEndDate(null);
		sessionJob.setStartDate(null);
		sessionJob.setResult(null);
		sessionJob.setEndStausCheckFlg(EndStatusCheckConstant.NO_WAIT_JOB);
		sessionJob.setDelayNotifyFlg(DelayNotifyConstant.NONE);

		//開始時保留、開始時スキップをチェック
		if(job.getSuspend().booleanValue()){
			//JobSessionJobの実行状態に保留中を設定
			sessionJob.setStatus(StatusConstant.TYPE_RESERVING);
		}else if(job.getSkip().booleanValue()){
			//JobSessionJobの実行状態にスキップを設定
			sessionJob.setStatus(StatusConstant.TYPE_SKIP);
		}else{
			//JobSessionJobの実行状態に待機を設定
			sessionJob.setStatus(StatusConstant.TYPE_WAIT);
		}

		if(job.getJobType() == JobConstant.TYPE_JOB
				|| job.getJobType() == JobConstant.TYPE_APPROVALJOB
				|| job.getJobType() == JobConstant.TYPE_MONITORJOB){
			//ジョブの場合
			List<JobSessionNodeEntity> nodeEntityList;
			if(facilityId != null && facilityId.length() > 0){
				// ノード詳細の場合
				nodeEntityList = Collections.<JobSessionNodeEntity>singletonList(QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId));
			} else {
				// ジョブ詳細の場合
				nodeEntityList = sessionJob.getJobSessionNodeEntities();
			}
			
			if(nodeEntityList != null) {
				for (JobSessionNodeEntity sessionNode : nodeEntityList) {
					// ジョブを中断から開始即時に遷移する場合、実行中のノード詳細は現在実行中のプロセスを
					// そのまま実行し続け、新規にプロセスを起こすことはないため、ノードが実行中の場合にはステータスクリアは行わない
					if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
						if(job.getJobType() == JobConstant.TYPE_APPROVALJOB){
							// 承認ジョブの場合、ノード状態は実行中でも承認状態が中断中に変わっているため、承認待に遷移させる
							sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_PENDING);
						}
						try {
							m_log.info("clearStatus() : skip clearStatus because running job is not completed. "
									+ "sessionId=" + sessionId + ", jobunitId=" + jobunitId
									+ ", jobId=" + jobId + ", facilityId=" + sessionNode.getId().getFacilityId());
						} catch (RuntimeException e) {
						}
						continue;
					}
					
					//セッションノードの実行状態に待機を設定
					sessionNode.setStatus(StatusConstant.TYPE_WAIT);

					//終了値、終了・中断日時、開始・再実行日時をクリア
					sessionNode.setEndValue(null);
					sessionNode.setEndDate(null);
					sessionNode.setStartDate(null);
					sessionNode.setRetryCount(0);
					sessionNode.setErrorRetryCount(0);
					sessionNode.setResult(null);
					sessionNode.setMessage(null);
					//承認情報をクリア
					sessionNode.setApprovalResult(null);
					sessionNode.setApprovalUser("");
				}
			}
		}else if(job.getJobType() == JobConstant.TYPE_FILEJOB && !CreateHulftJob.isHulftMode()){
			//HULFTではないファイル転送ジョブの場合

			//セッションIDとジョブIDから、直下のジョブを取得
			Collection<JobSessionJobEntity> collection = null;
			collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
			for (JobSessionJobEntity child : collection) {

				if(child.getId().getJobId().equals(jobId + CreateFileJob.FILE_LIST)) {
					//ジョブの実行状態をクリアする
					clearStatus(child.getId().getSessionId(), child.getId().getJobunitId(), child.getId().getJobId(), null);
				} else {
					//ジョブ削除を行う
					clearJob(child.getId().getSessionId(), child.getId().getJobunitId(), child.getId().getJobId());
				}
			}
		}else{
			//ジョブ以外の場合

			//セッションIDとジョブIDから、直下のジョブを取得
			Collection<JobSessionJobEntity> collection = null;
			collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
			for (JobSessionJobEntity child : collection) {
				//ジョブの実行状態をクリアする
				clearStatus(child.getId().getSessionId(), child.getId().getJobunitId(), child.getId().getJobId(), null);
			}
		}
	}

	/**
	 * ジョブの実行結果を削除します。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	private void clearJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		m_log.debug("clearJob() : sessionId=" + sessionId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		JobInfoEntity job = sessionJob.getJobInfoEntity();

		if(job.getJobType() != JobConstant.TYPE_JOB
				&& job.getJobType() != JobConstant.TYPE_APPROVALJOB
				&& job.getJobType() != JobConstant.TYPE_MONITORJOB){
			//ジョブ以外の場合

			//セッションIDとジョブIDから、直下のジョブを取得
			Collection<JobSessionJobEntity> collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
			for (JobSessionJobEntity relation : collection) {

				//ジョブを削除
				clearJob(relation.getId().getSessionId(), relation.getId().getJobunitId(), relation.getId().getJobId());
			}
		} else {
			List<JobSessionNodeEntity> list = sessionJob.getJobSessionNodeEntities();
			for (JobSessionNodeEntity sessionNode : list) {
				//実行中から他の状態に遷移した場合は、キャッシュを更新する。
				if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
					JpaTransactionManager jtm = new JpaTransactionManager();
					jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
				} else if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
					JobMultiplicityCache.removeWait(sessionNode.getId());
				}
			}
		}

		try {
			new NotifyControllerBean().deleteNotifyRelation(NotifyGroupIdGenerator.generate(job));
		} catch (HinemosUnknown e) {
			m_log.info("clearJob() : " + e.getMessage() + "," + job.getId());
		}


		// セッションジョブと関連するジョブ情報を削除
		sessionJob.unchain();	// 削除前処理
		em.remove(sessionJob);
	}

	/**
	 * 実行状態を設定します。
	 * 再帰的に親の実行状態も設定します。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @param status 実行状態
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	private void setStatus(String sessionId, String jobunitId, String jobId, Integer status) throws JobInfoNotFound, InvalidRole {
		m_log.debug("setStatus() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", status=" + status);

		// ジョブIDがトップだったら何もしない
		if (CreateJobSession.TOP_JOB_ID.equals(jobId)) {
			return;
		}

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		//実行状態を設定
		sessionJob.setStatus(status);

		//リレーションを取得し、親ジョブのジョブIDを取得
		String parentJobUnitId = sessionJob.getParentJobunitId();
		String parentJobId = sessionJob.getParentJobId();

		if(!CreateJobSession.TOP_JOB_ID.equals(parentJobId)){
			setStatus(sessionId, parentJobUnitId, parentJobId, status);
		}
	}
}
