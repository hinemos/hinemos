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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.bean.StatusConstant;

/**
 * ジョブ操作の保留に関する処理を行うクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperateWaitOfJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperateWaitOfJob.class );

	/**
	 * コンストラクタ
	 */
	public OperateWaitOfJob(){
		super();
	}

	/**
	 * ジョブを停止[保留]します。
	 * 
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 */
	public void waitJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("waitJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		if(sessionJob != null){
			//実行状態が待機の場合、実行状態を保留中にする
			if(sessionJob.getStatus() == StatusConstant.TYPE_WAIT){
				sessionJob.setStatus(StatusConstant.TYPE_RESERVING);
			}
		}
	}

	/**
	 * ジョブを開始[保留解除]します。
	 * 
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public void releaseWaitJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("releaseWaitJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		if(sessionJob != null){
			//実行状態が保留中の場合、実行状態を待機にする
			if(sessionJob.getStatus() == StatusConstant.TYPE_RESERVING){
				sessionJob.setStatus(StatusConstant.TYPE_WAIT);
			}
		}
	}
}
