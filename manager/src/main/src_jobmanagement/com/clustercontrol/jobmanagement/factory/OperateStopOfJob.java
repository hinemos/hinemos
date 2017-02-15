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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.model.JobEnvVariableInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.util.FromRunningAfterCommitCallback;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.jobmanagement.util.RunHistoryUtil;
import com.clustercontrol.jobmanagement.util.SendTopic;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブ操作の停止[コマンド]を行うクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperateStopOfJob {
	private static Log m_log = LogFactory.getLog( OperateStopOfJob.class );

	public OperateStopOfJob(){
		super();
	}

	/**
	 * ジョブを停止[コマンド],停止[状態変更]します。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws FacilityNotFound 
	 *
	 */
	public void stopJob(String sessionId, String jobunitId, String jobId) throws HinemosUnknown, JobInfoNotFound, InvalidRole, FacilityNotFound {
		m_log.info("stopJob() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		if(sessionJob.getStatus() != StatusConstant.TYPE_RUNNING){
			return;
		}

		//実行状態が実行中の場合、実行状態を停止処理中にする
		sessionJob.setStatus(StatusConstant.TYPE_STOPPING);

		JobInfoEntity job = sessionJob.getJobInfoEntity();
		if(job.getJobType() == JobConstant.TYPE_JOB
				|| job.getJobType() == JobConstant.TYPE_APPROVALJOB
				|| job.getJobType() == JobConstant.TYPE_MONITORJOB){
			//ノード停止処理
			for (JobSessionNodeEntity sessionNode : sessionJob.getJobSessionNodeEntities()) {
				stopNode(sessionId, jobunitId, jobId, sessionNode.getId().getFacilityId());
			}

			//全ノード停止チェック
			if(new JobSessionNodeImpl().checkAllNodeStop(sessionJob)){
				//全ノード停止の場合
				//実行状態にコマンド停止を設定
				sessionJob.setStatus(StatusConstant.TYPE_STOP);
				//ジョブ停止関連処理
				stopJob2(sessionId, jobunitId, jobId);
				//遅延通知状態を取得
				int flg = sessionJob.getDelayNotifyFlg();
				//遅延通知状態から操作済みフラグを取得
				int operationFlg = DelayNotifyConstant.getOperation(flg);
				if(operationFlg == DelayNotifyConstant.STOP_SET_END_VALUE){
					//操作済みフラグが停止[状態指定]の場合、停止[状態変更]を行う
					new OperateMaintenanceOfJob().maintenanceJob(
							sessionId, jobunitId, jobId,
							StatusConstant.TYPE_END_END_DELAY,
							job.getEndDelayOperationEndStatus(),
							job.getEndDelayOperationEndValue());
				}
			}
		} else {
			//セッションIDとジョブIDから、直下のジョブを取得（実行状態が実行中）
			Collection<JobSessionJobEntity> collection;
			collection = QueryUtil.getJobSessionJobByParentStatus(sessionId, jobunitId, jobId, StatusConstant.TYPE_RUNNING);
			for(JobSessionJobEntity child : collection) {
				//ジョブ停止処理を行う
				stopJob(child.getId().getSessionId(), child.getId().getJobunitId(), child.getId().getJobId());
			}
			//直下のジョブが全て停止したかチェック
			boolean endAll = true;
			collection = QueryUtil.getChildJobSessionJob(sessionId, jobunitId, jobId);
			for (JobSessionJobEntity child : collection) {
				int status = child.getStatus();
				child = null;
				//実行状態が停止処理中かチェック
				if(status == StatusConstant.TYPE_STOPPING ||
						status == StatusConstant.TYPE_RUNNING){
					endAll = false;
					break;
				}
			}

			//直下のジョブが全て停止の場合
			if(endAll){
				//実行状態をコマンド停止にする
				sessionJob.setStatus(StatusConstant.TYPE_STOP);
				if(job.getJobType() == JobConstant.TYPE_JOBNET || job.getJobType() == JobConstant.TYPE_FILEJOB){
					//遅延通知状態を取得
					int flg = sessionJob.getDelayNotifyFlg();
					//遅延通知状態から操作済みフラグを取得
					int operationFlg = DelayNotifyConstant.getOperation(flg);
					if(operationFlg == DelayNotifyConstant.STOP_SET_END_VALUE){
						//操作済みフラグが停止[状態指定]の場合、停止[状態変更]を行う
						new OperateMaintenanceOfJob().maintenanceJob(
								sessionId, jobunitId, jobId,
								StatusConstant.TYPE_END_END_DELAY,
								job.getEndDelayOperationEndStatus(),
								job.getEndDelayOperationEndValue());
					}
				}
			}
		}
	}

	/**
	 * ジョブ停止時関連処理を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 *
	 */
	protected void stopJob2(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.debug("stopJob2() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

		//リレーションを取得し、親ジョブのジョブIDを取得
		String parentJobUnitId = sessionJob.getParentJobunitId();
		String parentJobId = sessionJob.getParentJobId();

		//同一階層のジョブが全て停止したかチェック
		boolean endAll = true;
		Collection<JobSessionJobEntity> sameHierarchy = QueryUtil.getChildJobSessionJob(sessionId, parentJobUnitId, parentJobId);
		for (JobSessionJobEntity sessionJob1 : sameHierarchy) {

			int status = sessionJob1.getStatus();
			sessionJob1 = null;

			//実行状態が停止処理中かチェック
			if(status == StatusConstant.TYPE_STOPPING || status == StatusConstant.TYPE_RUNNING){
				endAll = false;
				break;
			}
		}

		//同一階層のジョブが全て完了の場合
		if(endAll && !CreateJobSession.TOP_JOB_ID.equals(parentJobId)){
			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity parentSessionJob = QueryUtil.getJobSessionJobPK(sessionId, parentJobUnitId, parentJobId);

			if(parentSessionJob.getStatus() == StatusConstant.TYPE_STOPPING){
				//実行状態にコマンド停止を設定
				parentSessionJob.setStatus(StatusConstant.TYPE_STOP);

				//ジョブ停止時関連処理（再帰呼び出し）
				stopJob2(sessionId, parentJobUnitId, parentJobId);

				JobInfoEntity job = parentSessionJob.getJobInfoEntity();
				if(job.getJobType() == JobConstant.TYPE_JOBNET || job.getJobType() == JobConstant.TYPE_FILEJOB){

					//遅延通知状態を取得
					int flg = parentSessionJob.getDelayNotifyFlg();
					//遅延通知状態から操作済みフラグを取得
					int operationFlg = DelayNotifyConstant.getOperation(flg);
					if(operationFlg == DelayNotifyConstant.STOP_SET_END_VALUE){
						//操作済みフラグが停止[状態指定]の場合、停止[状態変更]を行う
						new OperateMaintenanceOfJob().maintenanceJob(
								sessionId,
								parentJobUnitId,
								parentJobId,
								StatusConstant.TYPE_END_END_DELAY,
								job.getEndDelayOperationEndStatus(),
								job.getEndDelayOperationEndValue());
					}
				}
			}
		}
	}

	/**
	 * ノード停止処理を行います。
	 *
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound 
	 *
	 */
	public void stopNode(String sessionId, String jobunitId, String jobId, String facilityId) throws HinemosUnknown, JobInfoNotFound, InvalidRole, FacilityNotFound {
		m_log.info("stopNode() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		JobSessionNodeEntity stopNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);

		//実行状態が実行中(ノードに対する停止処理)、コマンド停止(ジョブに対する停止処理)の場合でなければ、
		//ノードに対する停止処理は無効とする。
		if(stopNode.getStatus() != StatusConstant.TYPE_RUNNING &&
				stopNode.getStatus() != StatusConstant.TYPE_STOPPING){
			m_log.info("not running : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId +
					", status=" + stopNode.getStatus());
			return;
		}

		JobInfoEntity job = sessionJob.getJobInfoEntity();
		List<JobSessionNodeEntity> sessionNodeList = null;
		if(facilityId != null && facilityId.length() > 0){
			JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);
			sessionNodeList = new ArrayList<JobSessionNodeEntity>();
			sessionNodeList.add(sessionNode);
		} else {
			sessionNodeList = sessionJob.getJobSessionNodeEntities();
		}

		//待ち条件ジョブ判定
		for (JobSessionNodeEntity sessionNode : sessionNodeList) {
			//実行状態が実行中の場合
			if(sessionNode.getStatus() == StatusConstant.TYPE_RUNNING){
				if(job.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS ||
						(job.getStopType() == CommandStopTypeConstant.EXECUTE_COMMAND &&
						job.getStopCommand() != null && job.getStopCommand().length() > 0)){
					//実行中から他の状態に遷移した場合は、キャッシュを更新する。
					if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
						JpaTransactionManager jtm = new JpaTransactionManager();
						jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
					} else if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
						JobMultiplicityCache.removeWait(sessionNode.getId());
					}
					//実行状態を停止処理中にする
					sessionNode.setStatus(StatusConstant.TYPE_STOPPING);
					//開始・再実行日時をクリア
					sessionNode.setStartDate(null);
					//メッセージをエージェント応答待ちに戻す
					if (job.getJobType() != JobConstant.TYPE_MONITORJOB
						&& job.getJobType() != JobConstant.TYPE_APPROVALJOB) {
						new JobSessionNodeImpl().setMessage(sessionNode, MessageConstant.WAIT_AGENT_RESPONSE.getMessage());
					}
					
					if (job.getJobType() == JobConstant.TYPE_APPROVALJOB) {
						//実行状態をコマンド停止にする
						sessionNode.setStatus(StatusConstant.TYPE_STOP);
						sessionNode.setApprovalStatus(JobApprovalStatusConstant.TYPE_STOP);
						new JobSessionNodeImpl().setMessage(sessionNode, MessageConstant.STOP_AT_ONCE.getMessage());
						// Topic送信しない
						continue;
					}

					if (job.getJobType() == JobConstant.TYPE_MONITORJOB) {
						// 監視ジョブの停止処理を行う
						sessionNode.setStatus(StatusConstant.TYPE_STOP);
						new JobSessionNodeImpl().setMessage(sessionNode, MessageConstant.STOP_AT_ONCE.getMessage());
						RunInstructionInfo runInstructionInfo = RunHistoryUtil.findRunHistory(
								sessionId, jobunitId, jobId, sessionNode.getId().getFacilityId());
						if (runInstructionInfo == null) {
							//実行指示情報を作成
							runInstructionInfo = new RunInstructionInfo();
							runInstructionInfo.setSessionId(sessionId);
							runInstructionInfo.setJobunitId(jobunitId);
							runInstructionInfo.setJobId(jobId);
							runInstructionInfo.setFacilityId(sessionNode.getId().getFacilityId());
							runInstructionInfo.setSpecifyUser(job.getSpecifyUser());
							runInstructionInfo.setUser(job.getEffectiveUser());
							runInstructionInfo.setCommandType(CommandTypeConstant.STOP);
							runInstructionInfo.setCommand(CommandConstant.MONITOR);
							runInstructionInfo.setStopType(job.getStopType());
						}
						MonitorJobWorker.runJob(runInstructionInfo);
						// Topic送信しない
						continue;
					}
					//実行指示情報を作成
					RunInstructionInfo instructionInfo = new RunInstructionInfo();
					instructionInfo.setSessionId(sessionId);
					instructionInfo.setJobunitId(jobunitId);
					instructionInfo.setJobId(jobId);
					instructionInfo.setFacilityId(sessionNode.getId().getFacilityId());
					instructionInfo.setSpecifyUser(job.getSpecifyUser());
					instructionInfo.setUser(job.getEffectiveUser());
					instructionInfo.setCommandType(CommandTypeConstant.STOP);
					instructionInfo.setStopType(job.getStopType());
					//環境変数情報の設定
					List<JobEnvVariableInfo> envInfoList = new ArrayList<JobEnvVariableInfo>();
					for(JobEnvVariableInfoEntity envEntity : job.getJobEnvVariableInfoEntities()) {
						JobEnvVariableInfo envInfo = new JobEnvVariableInfo();
						envInfo.setEnvVariableId(envEntity.getId().getEnvVariableId());
						envInfo.setValue(envEntity.getValue());
						envInfo.setDescription(envEntity.getDescription());
						envInfoList.add(envInfo);
					}
					instructionInfo.setJobEnvVariableInfoList(envInfoList);
					
					if (job.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
						m_log.info("stopNode() : Process Destroy");
						instructionInfo.setCommand("");
					} else {
						m_log.info("stopNode() : Send Stop Command");
						//コマンド内のパラメータを置き換える
						String stopCommand = ParameterUtil.replaceSessionParameterValue(
								sessionId, sessionNode.getId().getFacilityId(), job.getStopCommand());
						//コマンド内のパラメータを置き換える(#[RETURN:jobid:facilityId])
						stopCommand = ParameterUtil.replaceReturnCodeParameter(sessionId, jobunitId, stopCommand);
						instructionInfo.setCommand(stopCommand);
					}

					try {
						//Topicに送信
						SendTopic.put(instructionInfo);
					} catch (Exception e) {
						m_log.warn("stopNode() RunInstructionInfo(command type:STOP) send error : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					}
				}else{
					//実行中から他の状態に遷移した場合は、キャッシュを更新する。
					if (sessionNode.getStatus() == StatusConstant.TYPE_RUNNING) {
						JpaTransactionManager jtm = new JpaTransactionManager();
						jtm.addCallback(new FromRunningAfterCommitCallback(sessionNode.getId()));
					} else if (sessionNode.getStatus() == StatusConstant.TYPE_WAIT) {
						JobMultiplicityCache.removeWait(sessionNode.getId());
					}
					//実行状態をコマンド停止にする
					sessionNode.setStatus(StatusConstant.TYPE_STOP);
				}
			}
		}
	}
}
