/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
 * ジョブ操作のスキップに関する処理を行うクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperateSkipOfJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperateSkipOfJob.class );

	/**
	 * コンストラクタ
	 */
	public OperateSkipOfJob(){
		super();
	}

	/**
	 * ジョブを開始[スキップ解除]します。
	 * 
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public void releaseSkipJob(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("releaseSkipJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		if(sessionJob != null){
			//実行状態がスキップの場合
			if(sessionJob.getStatus() == StatusConstant.TYPE_SKIP){
				//実行状態を待機にする
				sessionJob.setStatus(StatusConstant.TYPE_WAIT);
				//終了値をクリア
				sessionJob.setEndValue(null);
			}
		}
	}

	/**
	 * ジョブを停止[スキップ]します。
	 * 
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param endValue 終了値
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public void skipJob(
			String sessionId,
			String jobunitId,
			String jobId) throws JobInfoNotFound, InvalidRole {
		m_log.debug("skipJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId  + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		if(sessionJob != null){
			//実行状態が待機の場合、実行状態をスキップにする
			if(sessionJob.getStatus() == StatusConstant.TYPE_WAIT){
				sessionJob.setStatus(StatusConstant.TYPE_SKIP);
			}

		}
	}
}
