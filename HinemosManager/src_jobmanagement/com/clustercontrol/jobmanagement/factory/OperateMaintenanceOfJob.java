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

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.util.FromRunningAfterCommitCallback;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * ジョブ操作の停止[状態変更]を行うクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperateMaintenanceOfJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperateMaintenanceOfJob.class );

	/**
	 * コンストラクタ
	 */
	public OperateMaintenanceOfJob(){
		super();
	}

	/**
	 * ジョブを停止[状態変更]します。
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
	public void maintenanceNode(
			String sessionId,
			String jobunitId,
			String jobId,
			String facilityId,
			Integer endValue) throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("maintenanceJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId + ", endValue=" + endValue);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);

		if(sessionNode != null){
			//実行中から他の状態に遷移した場合は、キャッシュを更新する。
			if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
				JpaTransactionManager jtm = new JpaTransactionManager();
				jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
			} else if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
				JobMultiplicityCache.removeWait(sessionNode.getId());
			}
			//実行状態を変更済にする
			sessionNode.setStatus(StatusConstant.TYPE_MODIFIED);
			//終了値を設定
			sessionNode.setEndValue(endValue);
			//終了日時を設定
			sessionNode.setEndDate(HinemosTime.currentTimeMillis());
			//ジョブ終了時関連処理
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
	}


	/**
	 * ジョブを停止[状態変更]します。
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
	public void maintenanceJob(
			String sessionId,
			String jobunitId,
			String jobId,
			Integer status,
			Integer endStatus,
			Integer endValue) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("maintenanceJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId +
				", endStatus=" + endStatus + ", endValue=" + endValue);


		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		if(sessionJob != null){
			//実行状態を変更済にする
			sessionJob.setStatus(status);
			//終了状態を設定
			sessionJob.setEndStatus(endStatus);
			//終了値を設定
			sessionJob.setEndValue(endValue);
			//終了日時を設定
			sessionJob.setEndDate(HinemosTime.currentTimeMillis());
			//ジョブ終了時関連処理
			new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, null, false);
		}
	}
}
