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

import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.util.FromRunningAfterCommitCallback;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 *  ジョブ操作の停止[強制]を行うクラスです。
 *
 *
 */
public class OperateForceStopOfJob {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperateForceStopOfJob.class );
	/**
	 * コンストラクタ
	 */
	public OperateForceStopOfJob(){
		super();
	}

	/**
	 * ジョブを停止[強制]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param endValue 終了値
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void forceStopNode(
			String sessionId,
			String jobunitId,
			String jobId,
			String facilityId,
			Integer endValue) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("forceStopJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId +
				", facilityId=" + facilityId + ", endValue=" + endValue);

		forceStopNode2(sessionId, jobunitId, jobId, facilityId, endValue);

		//ジョブ終了チェック
		try {
			new JobSessionNodeImpl().endNodeFinish(sessionId, jobunitId, jobId, facilityId, null, null);
		} catch (EntityExistsException e) {
			// TODO
			// throwするExceptionをHinemosUnknownでラップしているが、後で修正すること。
			m_log.warn("maintenanceNode " + e, e);
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (FacilityNotFound e) {
			m_log.warn("maintenanceNode " + e, e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	private void forceStopNode2(
			String sessionId,
			String jobunitId,
			String jobId,
			String facilityId,
			Integer endValue) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("forceStopJob2() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId +
				", facilityId=" + facilityId + ", endValue=" + endValue);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);

		// 強制終了の対象となるのは待機と停止処理中のみ。（参考：JobOperationJudgement.java）
		if(sessionNode.getStatus() != StatusConstant.TYPE_WAIT && sessionNode.getStatus() != StatusConstant.TYPE_STOPPING){
			return;
		}

		//実行中から他の状態に遷移した場合は、キャッシュを更新する。
		if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
			JpaTransactionManager jtm = new JpaTransactionManager();
			jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
		} else if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
			JobMultiplicityCache.removeWait(sessionNode.getId());
		}
		//実行状態を終了にする
		sessionNode.setStatus(StatusConstant.TYPE_END);
		//終了値を設定
		sessionNode.setEndValue(endValue);
		//終了日時を設定
		sessionNode.setEndDate(HinemosTime.currentTimeMillis());
		//強制終了
		new JobSessionNodeImpl().setMessage(sessionNode, MessageConstant.JOB_STOP_FORCE.getMessage());
	}

	/**
	 * ジョブを停止[強制]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param endValue 終了値
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public void forceStopJob(
			String sessionId,
			String jobunitId,
			String jobId,
			Integer endStatus,
			Integer endValue) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("forceStopJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId +
				", endValue=" + endValue);

		forceStopJob2(sessionId, jobunitId, jobId, endStatus, endValue);

		//ジョブ終了時関連処理(後続ジョブを動作させる。)
		new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, null, false);
	}

	private void forceStopJob2(
			String sessionId,
			String jobunitId,
			String jobId,
			Integer endStatus,
			Integer endValue) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("forceStopJob2() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId +
				", endValue=" + endValue);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		// 強制終了の対象となるのは待機と停止処理中のみ。（参考：JobOperationJudgement.java）
		if(sessionJob.getStatus() != StatusConstant.TYPE_WAIT && sessionJob.getStatus() != StatusConstant.TYPE_STOPPING){
			return;
		}

		if (sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOB
				&& sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_APPROVALJOB
				&& sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_MONITORJOB) {
			// 配下のジョブを停止する。
			List<JobSessionJobEntity> childJob = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
			for (JobSessionJobEntity job : childJob) {
				forceStopJob2(sessionId, jobunitId, job.getId().getJobId(), endStatus, endValue);
			}
		} else {
			// 配下のノードを停止する。
			List<JobSessionNodeEntity> nodeJobList = sessionJob.getJobSessionNodeEntities();
			for (JobSessionNodeEntity node : nodeJobList) {
				if (!StatusConstant.isEndGroup(node.getStatus())){
					forceStopNode2(sessionId, jobunitId, jobId, node.getId().getFacilityId(), endValue);
				}
			}
		}
		//自身の状態を変更する。
		sessionJob.setStatus(StatusConstant.TYPE_END);
		sessionJob.setEndStatus(endStatus);
		sessionJob.setEndValue(endValue);
		sessionJob.setEndDate(HinemosTime.currentTimeMillis());
	}
}
