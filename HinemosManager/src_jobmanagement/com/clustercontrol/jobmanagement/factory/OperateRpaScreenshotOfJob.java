/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.rpa.bean.RoboInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RpaScreenshotTriggerTypeConstant;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.jobmanagement.util.SendTopic;
import com.clustercontrol.util.HinemosTime;

/**
 * ジョブ操作のスクリーンショット取得を行うクラスです。
 */
public class OperateRpaScreenshotOfJob {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(OperateRpaScreenshotOfJob.class);

	/**
	 * コンストラクタ
	 */
	public OperateRpaScreenshotOfJob() {
	}

	/**
	 * スクリーンショット取得を実行します。
	 * 
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param triggerType 取得契機
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws JobInfoNotFound
	 */
	public void takeScreenshot(String sessionId, String jobunitId, String jobId, String facilityId, int triggerType)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.info("takeScreenshot() : sessionId=" + sessionId + ", jobunitid=" + jobunitId + ", jobId=" + jobId
				+ ", facilityId=" + facilityId);
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
		if (sessionNode.getStatus() != StatusConstant.TYPE_RUNNING  && 
			!( triggerType == RpaScreenshotTriggerTypeConstant.END_DELAY 
				&& sessionNode.getStatus() == StatusConstant.TYPE_STOPPING 
			) 
		) {
			m_log.warn("takeScreenshot() : node status isn't running, skip status=" + sessionNode.getStatus());
			return; // 実行中でない場合は処理を行わない（終了遅延による停止処理中は除く） 
		}

		// 実行指示情報を作成
		RunInstructionInfo instructionInfo = new RunInstructionInfo();
		instructionInfo.setSessionId(sessionJob.getId().getSessionId());
		instructionInfo.setJobunitId(sessionJob.getId().getJobunitId());
		instructionInfo.setJobId(sessionJob.getId().getJobId());
		instructionInfo.setFacilityId(sessionNode.getId().getFacilityId());
		instructionInfo.setCommand(CommandConstant.RPA);
		instructionInfo.setCommandType(CommandTypeConstant.SCREENSHOT);
		instructionInfo.setRpaScreenshotTriggerType(triggerType);

		// RPAツールエグゼキューターシナリオ実行情報を設定
		RoboRunInfo roboRunInfo = new RoboRunInfo(
				new RoboInfo(HinemosTime.currentTimeMillis(), job.getId().getSessionId(), job.getId().getJobunitId(),
						job.getId().getJobId(), facilityId, job.getRpaLoginUserId()));
		m_log.debug("roboRunInfo=" + roboRunInfo);
		instructionInfo.setRpaRoboRunInfo(roboRunInfo);

		try {
			// Topicに送信
			SendTopic.put(instructionInfo);
		} catch (Exception e) {
			m_log.warn("takeScreenshot() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ジョブ配下のノードについてスクリーンショットの取得を実行します。
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @param triggerType 取得契機
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void takeScreenshot(String sessionId, String jobunitId, String jobId, int triggerType)
			throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		m_log.info("takeScreenshot() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
		// セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
			takeScreenshot(sessionId, jobunitId, jobId, sessionNode.getId().getFacilityId(), triggerType);
		}
	}
}
