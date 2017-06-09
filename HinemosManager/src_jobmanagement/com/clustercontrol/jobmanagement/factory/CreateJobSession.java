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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.EndStatusCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParam;
import com.clustercontrol.jobmanagement.bean.JobSessionRequestMessage;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.model.JobCommandParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobCommandParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobStartParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableInfoEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableMstEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobMstEntity;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ジョブの実行用情報を作成するクラスです。
 *
 * @version 1.0.0
 * @since 2.0.0
 */
public class CreateJobSession {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( CreateJobSession.class );

	/** ツリートップのID */
	public static final String TOP_JOBUNIT_ID = "_ROOT_";
	public static final String TOP_JOB_ID = "TOP";

	/**
	 * ジョブの実行用情報を階層に従い作成します。<BR>
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param info ログ出力情報
	 * @param triggerInfo 実行契機情報
	 * @return セッションID
	 * @throws FacilityNotFound
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws JobSessionDuplicate
	 * @throws InvalidRole
	 *
	 */
	public static String makeSession(String jobunitId, String jobId, OutputBasicInfo info, JobTriggerInfo triggerInfo)
			throws HinemosUnknown {
		String sessionId = CreateSessionId.create();
		m_log.debug("makeSession() sessionId=" + sessionId);
		
		JobSessionRequestMessage message = new JobSessionRequestMessage(sessionId, jobunitId, jobId, info, triggerInfo);
		AsyncWorkerPlugin.addTask(CreateJobSessionTaskFactory.class.getSimpleName(), message, true);

		return sessionId;
	}
	


	/**
	 * ジョブの実行用情報を作成します。
	 * <p>
	 * <ol>
	 * <li>セッションジョブを作成します。</li>
	 * <li>ジョブリレーション情報を作成します。</li>
	 * <li>ジョブ情報を作成します。</li>
	 * <li>ジョブ待ち条件情報を作成します。</li>
	 * <li>ジョブコマンド情報を作成します。</li>
	 * </ol>
	 *
	 * @param job ファイル転送ジョブのジョブマスタ
	 * @param sessionId セッションID
	 * @return
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws RoleNotFound 
	 */
	public static JobSessionJobEntityPK createJobSessionJob(JobMstEntity job, String sessionId, OutputBasicInfo info, boolean first, JobTriggerInfo triggerInfo, JobMstEntity referJob, HashMap<String, String> jobIdMap)
			throws JobInfoNotFound, FacilityNotFound, EntityExistsException, HinemosUnknown, JobMasterNotFound, InvalidRole, RoleNotFound {

		m_log.debug("createJobSessionJob() sessionId=" + sessionId + " first=" + first);
		
		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();

		String jobunitId = job.getId().getJobunitId();
		String jobId = job.getId().getJobId();
		
		//JobSessionEntityを設定
		JobSessionEntity jobSessionEntity = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
		//親ジョブを設定
		JobSessionJobEntity parentJobSessionJobEntity = null;
		
		String parentJobunitId = job.getParentJobunitId();
		String parentJobId = job.getParentJobId();
		//親ジョブが参照ジョブネットの場合、参照先ジョブネットのジョブIDではなく
		//参照ジョブネットのジョブIDを指定する必要がある(セッションIDが参照ジョブネットのジョブIDで紐づくため)
		//ただし、孫ジョブの親には自身(子ジョブ)のジョブID(リネーム後)を設定する必要があるため、referJobをnullクリアする
		//(参照ジョブの場合は親ジョブとなることがないため、referJobがパラメタ設定されるのは、親ジョブが参照ジョブネットの場合のみ)
		if (referJob != null) {
			m_log.debug("createJobSessionJob() referJob jobunitId=" + referJob.getId().getJobunitId() + " jobId=" + referJob.getId().getJobId());
			parentJobunitId = referJob.getId().getJobunitId();
			parentJobId = referJob.getId().getJobId();
			referJob = null;
		} else if(jobIdMap != null && !jobIdMap.isEmpty()){
			//上位ジョブが参照先ジョブネット配下のジョブの場合、リネーム後のジョブIDを設定する
			parentJobId = jobIdMap.get(job.getParentJobId());
			m_log.debug("rename jobId at parentJobSessionJobEntity:" + job.getParentJobId() + "->" + parentJobId);
		}
		
		if(first) {
			parentJobSessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, TOP_JOBUNIT_ID, TOP_JOB_ID);
		} else {
			parentJobSessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, parentJobunitId, parentJobId);
		}
		
		//参照ジョブ/参照ジョブネットの場合、UnitIdとjobID以外を参照先のジョブとして設定する
		if(job.getJobType() == JobConstant.TYPE_REFERJOB || job.getJobType() == JobConstant.TYPE_REFERJOBNET){
			referJob = job;
			//参照先のジョブマスタを取得する
			job = QueryUtil.getJobMstPK(job.getReferJobUnitId(), job.getReferJobId());
		}
		
		//参照ジョブネットの場合、参照先ジョブネット配下のジョブIDが重複しないようにリネームする
		if(referJob != null && referJob.getJobType() == JobConstant.TYPE_REFERJOBNET){
			// ジョブネット配下のジョブのリネーム情報は以下で一括取得する(リネーム文字列のプレフィックスは参照JOBネットのジョブID)
			// 参照先配下には参照ジョブネットは登録出来ないため、リネーム情報の作成は参照ジョブネット毎に1度のみとなる
			jobIdMap = getRenameJobIdMap(referJob.getId().getJobId(), job, null);
		}
		
		//JobSessionJobを作成
		//jobIdMapにリネーム情報がある場合、上位が参照ジョブネットであるためjobIdをリネームする
		//リネームリスト内のjobと同名のjobIdの場合、リネーム後のjobIdでJobSessionJobEntity,JobInfoEntityを作成する
		if(jobIdMap != null && !jobIdMap.isEmpty() && jobIdMap.get(jobId) != null){
			jobId = jobIdMap.get(jobId);
			m_log.debug("rename jobId at JobSessionJobEntity:" + job.getId().getJobId() + "->" + jobId);
		}
		
		JobSessionJobEntity jobSessionJobEntity = new JobSessionJobEntity(
				jobSessionEntity,
				jobunitId,
				jobId);
				// 重複チェック
		jtm.checkEntityExists(JobSessionJobEntity.class, jobSessionJobEntity.getId());
		jobSessionJobEntity.setParentJobunitId(parentJobSessionJobEntity.getId().getJobunitId());
		jobSessionJobEntity.setParentJobId(parentJobSessionJobEntity.getId().getJobId());
		jobSessionJobEntity.setStartDate(null);
		jobSessionJobEntity.setEndDate(null);
		jobSessionJobEntity.setEndStatus(null);
		jobSessionJobEntity.setResult(null);
		jobSessionJobEntity.setEndStausCheckFlg(EndStatusCheckConstant.NO_WAIT_JOB);
		jobSessionJobEntity.setDelayNotifyFlg(DelayNotifyConstant.NONE);
		jobSessionJobEntity.setEndValue(null);

		//実行状態・終了値を設定
		if (first) {
			// 最上位の保留とスキップは無視する。
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
		} else if(job.getSuspend() != null && job.getSuspend().booleanValue()) {
			//保留
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_RESERVING);
		} else if(job.getSkip() != null && job.getSkip().booleanValue()) {
			//スキップ
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_SKIP);
		} else {
			//保留・スキップ以外
			jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
		}

		//JobInfoEntityを作成
		JobInfoEntity jobInfoEntity = new JobInfoEntity(jobSessionJobEntity);
		// 重複チェック
		jtm.checkEntityExists(JobInfoEntity.class, jobInfoEntity.getId());
		jobInfoEntity.setJobName(job.getJobName());
		jobInfoEntity.setDescription(job.getDescription());
		jobInfoEntity.setJobType(job.getJobType());
		jobInfoEntity.setRegisteredModule(job.isRegisteredModule());
		jobInfoEntity.setRegDate(job.getRegDate());
		jobInfoEntity.setUpdateDate(job.getUpdateDate());
		jobInfoEntity.setRegUser(job.getRegUser());
		jobInfoEntity.setUpdateUser(job.getUpdateUser());
		jobInfoEntity.setIconId(job.getIconId());

		//ジョブネット・ジョブ・ファイル転送ジョブ・承認ジョブ・監視ジョブの場合
		//待ち条件を設定
		if(job.getJobType() == JobConstant.TYPE_JOBNET ||
				job.getJobType() == JobConstant.TYPE_APPROVALJOB ||
				job.getJobType() == JobConstant.TYPE_JOB ||
				job.getJobType() == JobConstant.TYPE_FILEJOB ||
				job.getJobType() == JobConstant.TYPE_MONITORJOB) {
			//参照ジョブの場合、待ち条件のみ参照元を使用する
			JobMstEntity tmp = null;
			if (referJob != null) {
				tmp = referJob;
			} else {
				tmp = job;
			}
			jobInfoEntity.setConditionType(tmp.getConditionType());
			jobInfoEntity.setUnmatchEndFlg(tmp.getUnmatchEndFlg());
			jobInfoEntity.setUnmatchEndStatus(tmp.getUnmatchEndStatus());
			jobInfoEntity.setUnmatchEndValue(tmp.getUnmatchEndValue());
			jobInfoEntity.setSkipEndStatus(job.getSkipEndStatus());
			jobInfoEntity.setSkipEndValue(job.getSkipEndValue());
			jobInfoEntity.setCalendarId(job.getCalendarId());
			jobInfoEntity.setCalendarEndStatus(job.getCalendarEndStatus());
			jobInfoEntity.setCalendarEndValue(job.getCalendarEndValue());
			jobInfoEntity.setStartDelaySession(job.getStartDelaySession());
			jobInfoEntity.setStartDelaySessionValue(job.getStartDelaySessionValue());
			jobInfoEntity.setStartDelayTime(job.getStartDelayTime());
			jobInfoEntity.setStartDelayTimeValue(job.getStartDelayTimeValue());
			jobInfoEntity.setStartDelayConditionType(job.getStartDelayConditionType());
			jobInfoEntity.setStartDelayNotify(job.getStartDelayNotify());
			jobInfoEntity.setStartDelayNotifyPriority(job.getStartDelayNotifyPriority());
			jobInfoEntity.setStartDelayOperation(job.getStartDelayOperation());
			jobInfoEntity.setStartDelayOperationType(job.getStartDelayOperationType());
			jobInfoEntity.setStartDelayOperationEndStatus(job.getStartDelayOperationEndStatus());
			jobInfoEntity.setStartDelayOperationEndValue(job.getStartDelayOperationEndValue());
			jobInfoEntity.setEndDelaySession(job.getEndDelaySession());
			jobInfoEntity.setEndDelaySessionValue(job.getEndDelaySessionValue());
			jobInfoEntity.setEndDelayJob(job.getEndDelayJob());
			jobInfoEntity.setEndDelayJobValue(job.getEndDelayJobValue());
			jobInfoEntity.setEndDelayTime(job.getEndDelayTime());
			jobInfoEntity.setEndDelayTimeValue(job.getEndDelayTimeValue());
			jobInfoEntity.setEndDelayConditionType(job.getEndDelayConditionType());
			jobInfoEntity.setEndDelayNotify(job.getEndDelayNotify());
			jobInfoEntity.setEndDelayNotifyPriority(job.getEndDelayNotifyPriority());
			jobInfoEntity.setEndDelayOperation(job.getEndDelayOperation());
			jobInfoEntity.setEndDelayOperationType(job.getEndDelayOperationType());
			jobInfoEntity.setEndDelayOperationEndStatus(job.getEndDelayOperationEndStatus());
			jobInfoEntity.setEndDelayOperationEndValue(job.getEndDelayOperationEndValue());

			jobInfoEntity.setMultiplicityNotify(job.getMultiplicityNotify());
			jobInfoEntity.setMultiplicityNotifyPriority(job.getMultiplicityNotifyPriority());
			jobInfoEntity.setMultiplicityOperation(job.getMultiplicityOperation());
			jobInfoEntity.setMultiplicityEndValue(job.getMultiplicityEndValue());
		}

		//実行コマンドを設定
		if(job.getJobType() == JobConstant.TYPE_JOB){
			jobInfoEntity.setFacilityId(job.getFacilityId());
			jobInfoEntity.setProcessMode(job.getProcessMode());
			if (triggerInfo.getJobCommand()) {
				jobInfoEntity.setStartCommand(triggerInfo.getJobCommandText());
			} else {
				jobInfoEntity.setStartCommand(job.getStartCommand());
			}
			jobInfoEntity.setStopType(job.getStopType());
			jobInfoEntity.setStopCommand(job.getStopCommand());
			jobInfoEntity.setSpecifyUser(job.getSpecifyUser());
			jobInfoEntity.setEffectiveUser(job.getEffectiveUser());
			jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
			jobInfoEntity.setArgumentJobId(job.getArgumentJobId());
			jobInfoEntity.setArgument(job.getArgument());
			jobInfoEntity.setMessageRetry(job.getMessageRetry());
			jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
			jobInfoEntity.setCommandRetry(job.getCommandRetry());

			// JobCommandParamInfoEntityの作成
			List<JobCommandParamMstEntity> JobCommandParamEntityList = job.getJobCommandParamEntities();
			if (JobCommandParamEntityList != null) {
				List<JobCommandParamInfoEntity> jobCommandParamInfoEntityList = new ArrayList<>();
				for(JobCommandParamMstEntity jobCommandParamEntity : JobCommandParamEntityList) {
					// インスタンス生成
					JobCommandParamInfoEntity jobCommandParamInfoEntity = new JobCommandParamInfoEntity(
							jobInfoEntity,
							jobInfoEntity.getId().getJobunitId(),
							jobInfoEntity.getId().getJobId(),
							jobCommandParamEntity.getId().getParamId());
					jobCommandParamInfoEntity.setJobStandardOutputFlg(jobCommandParamEntity.getJobStandardOutputFlg());
					jobCommandParamInfoEntity.setValue(jobCommandParamEntity.getValue());
					jobCommandParamInfoEntityList.add(jobCommandParamInfoEntity);
				}
				jobInfoEntity.setJobCommandParamInfoEntities(jobCommandParamInfoEntityList);
			}
			jobInfoEntity.setManagerDistribution(job.getManagerDistribution());
			jobInfoEntity.setScriptName(job.getScriptName());
			jobInfoEntity.setScriptEncoding(job.getScriptEncoding());
			jobInfoEntity.setScriptContent(job.getScriptContent());
			
			// 環境変数の設定
			List<JobEnvVariableMstEntity> jobEnvVariableMstEntityList = job.getJobEnvVariableMstEntities();
			if (jobEnvVariableMstEntityList != null) {
				for(JobEnvVariableMstEntity jobEnvVariableMstEntity : jobEnvVariableMstEntityList) {
					// インスタンス生成
					JobEnvVariableInfoEntity jobEnvVariableInfoEntity
					= new JobEnvVariableInfoEntity(
							jobInfoEntity,
							jobEnvVariableMstEntity.getId().getEnvVariableId());
					// 重複チェック
					jtm.checkEntityExists(JobEnvVariableInfoEntity.class, jobEnvVariableInfoEntity.getId());
					jobEnvVariableInfoEntity.setValue(jobEnvVariableMstEntity.getValue());
					jobEnvVariableInfoEntity.setDescription(jobEnvVariableMstEntity.getDescription());
				}
			}
		}

		//ファイル転送情報を設定
		if(job.getJobType() == JobConstant.TYPE_FILEJOB){
			jobInfoEntity.setProcessMode(job.getProcessMode());
			jobInfoEntity.setSrcFacilityId(job.getSrcFacilityId());
			jobInfoEntity.setDestFacilityId(job.getDestFacilityId());
			jobInfoEntity.setSrcFile(job.getSrcFile());
			jobInfoEntity.setSrcWorkDir(job.getSrcWorkDir());
			jobInfoEntity.setDestDirectory(job.getDestDirectory());
			jobInfoEntity.setDestWorkDir(job.getDestWorkDir());
			jobInfoEntity.setCompressionFlg(job.getCompressionFlg());
			jobInfoEntity.setCheckFlg(job.getCheckFlg());
			jobInfoEntity.setSpecifyUser(job.getSpecifyUser());
			jobInfoEntity.setEffectiveUser(job.getEffectiveUser());
			jobInfoEntity.setMessageRetry(job.getMessageRetry());
			jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
			jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
			jobInfoEntity.setCommandRetry(job.getCommandRetry());
		}
		
		//承認情報を設定
		if(job.getJobType() == JobConstant.TYPE_APPROVALJOB){
			jobInfoEntity.setApprovalReqRoleId(job.getApprovalReqRoleId());
			jobInfoEntity.setApprovalReqUserId(job.getApprovalReqUserId());
			jobInfoEntity.setApprovalReqSentence(job.getApprovalReqSentence());
			jobInfoEntity.setApprovalReqMailTitle(job.getApprovalReqMailTitle());
			jobInfoEntity.setApprovalReqMailBody(job.getApprovalReqMailBody());
			jobInfoEntity.setUseApprovalReqSentence(job.isUseApprovalReqSentence());
			// ファシリティID設定(承認ジョブの場合はオーナーロールID)
			jobInfoEntity.setFacilityId(jobSessionJobEntity.getOwnerRoleId());
			// ノード処理で必要な設定のみ初期値設定
			jobInfoEntity.setProcessMode(ProcessingMethodConstant.TYPE_ALL_NODE);
			jobInfoEntity.setMessageRetryEndFlg(false);
			jobInfoEntity.setMessageRetry(1);
			jobInfoEntity.setCommandRetryFlg(false);
			jobInfoEntity.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
			jobInfoEntity.setStopCommand("");
		}

		//監視ジョブ情報を設定
		if(job.getJobType() == JobConstant.TYPE_MONITORJOB){
			jobInfoEntity.setFacilityId(job.getFacilityId());
			jobInfoEntity.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
			jobInfoEntity.setProcessMode(job.getProcessMode());
			jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
			jobInfoEntity.setArgumentJobId(job.getArgumentJobId());
			jobInfoEntity.setArgument(job.getArgument());
			jobInfoEntity.setMessageRetry(job.getMessageRetry());
			jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
			jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
			jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
			jobInfoEntity.setCommandRetry(job.getCommandRetry());
			jobInfoEntity.setMonitorId(job.getMonitorId());
			jobInfoEntity.setMonitorInfoEndValue(job.getMonitorInfoEndValue());
			jobInfoEntity.setMonitorWarnEndValue(job.getMonitorWarnEndValue());
			jobInfoEntity.setMonitorCriticalEndValue(job.getMonitorCriticalEndValue());
			jobInfoEntity.setMonitorUnknownEndValue(job.getMonitorUnknownEndValue());
			jobInfoEntity.setMonitorWaitTime(job.getMonitorWaitTime());
			jobInfoEntity.setMonitorWaitEndValue(job.getMonitorWaitEndValue());
		}

		// 待ち条件を設定
		// first:最上位のジョブ以外は待ち条件、制御(suspend,skip,calendar)、開始遅延、終了遅延を設定する。
		if (!first) {
			jobInfoEntity.setSuspend(job.getSuspend());
			jobInfoEntity.setSkip(job.getSkip());
			jobInfoEntity.setCalendar(job.getCalendar());
			jobInfoEntity.setStartDelay(job.getStartDelay());
			jobInfoEntity.setEndDelay(job.getEndDelay());
			//参照ジョブの場合、待ち条件のみ参照元を使用する
			JobMstEntity tmp = null;
			if (referJob != null) {
				tmp = referJob;
			} else {
				tmp = job;
			}
			//待ち条件－時刻(ジョブ手動起動時のダイアログで「無視する」を有効にした場合はNULL設定)
			if (triggerInfo.getJobWaitTime()) {
				jobInfoEntity.setStartTime(null);
			} else {
				jobInfoEntity.setStartTime(tmp.getStartTime());
				jobInfoEntity.setStartTimeDescription(tmp.getStartTimeDescription());
				m_log.debug("getStartTime = " + tmp.getStartTime());
				m_log.debug("getStartTimeDescription = " + tmp.getStartTimeDescription());
			}

			//待ち条件－セッション開始時の時間（分）(ジョブ手動起動時ダイアログで「無視する」を有効にした場合はNULL設定)
			if (triggerInfo.getJobWaitMinute()) {
				jobInfoEntity.setStartMinute(null);
			} else {
				jobInfoEntity.setStartMinute(tmp.getStartMinute());
				jobInfoEntity.setStartMinuteDescription(tmp.getStartMinuteDescription());
				m_log.debug("getStartMinute = " + tmp.getStartTime());
				m_log.debug("getStartTimeDescription = " + tmp.getStartMinuteDescription());
			}

			//JobStartJobInfoEntityを作成
			List<JobStartJobMstEntity> jobStartJobMstEntityList = tmp.getJobStartJobMstEntities();
			if (jobStartJobMstEntityList != null) {
				for(JobStartJobMstEntity jobStartJobMstEntity : jobStartJobMstEntityList) {
					//jobIdMapにリネーム情報がある場合、、上位が参照ジョブネットであるため待ち条件内のjobIdをリネームする
					//待ち条件に指定するjobIdが、リネームリスト内のjobと同名のjobIdの場合、リネーム後のjobIdでJobStartJobInfoEntityを作成する
					String renameTargetJobId = null;
					if(jobIdMap != null && !jobIdMap.isEmpty()){
						renameTargetJobId = jobIdMap.get(jobStartJobMstEntity.getId().getTargetJobId());
						m_log.debug("rename jobId at JobStartJobMstEntity:" + jobStartJobMstEntity.getId().getTargetJobId() + "->" + renameTargetJobId);
					}
					// インスタンス生成
					JobStartJobInfoEntity jobStartJobInfoEntity
					= new JobStartJobInfoEntity(
							jobInfoEntity,
							jobStartJobMstEntity.getId().getTargetJobunitId(),
							(renameTargetJobId != null ? renameTargetJobId : jobStartJobMstEntity.getId().getTargetJobId()),
							jobStartJobMstEntity.getId().getTargetJobType(),
							jobStartJobMstEntity.getId().getTargetJobEndValue());
					jobStartJobInfoEntity.setTargetJobDescription(jobStartJobMstEntity.getTargetJobDescription());
					m_log.debug("getTargetJobType = " + jobStartJobMstEntity.getId().getTargetJobType());
					m_log.debug("getTargetJobId = " + (renameTargetJobId != null ? renameTargetJobId : jobStartJobMstEntity.getId().getTargetJobId()));
					m_log.debug("getTargetJobEndValue = " + jobStartJobMstEntity.getId().getTargetJobEndValue());
					m_log.debug("getTargetJobDescription = " + jobStartJobMstEntity.getTargetJobDescription());
					// 重複チェック
					jtm.checkEntityExists(JobStartJobInfoEntity.class, jobStartJobInfoEntity.getId());
				}
			}

			//JobStartParamMstEntityを作成
			List<JobStartParamMstEntity> jobStartParamMstEntityList = tmp.getJobStartParamMstEntities();
			if (jobStartParamMstEntityList != null) {
				for (JobStartParamMstEntity jobStartParamMstEntity : jobStartParamMstEntityList) {
					JobStartParamInfoEntity jobStartParamInfoEntity = new JobStartParamInfoEntity(
							jobInfoEntity,
							jobStartParamMstEntity.getId().getStartDecisionValue01(),
							jobStartParamMstEntity.getId().getStartDecisionValue02(),
							jobStartParamMstEntity.getId().getTargetJobType(),
							jobStartParamMstEntity.getId().getStartDecisionCondition());
					jobStartParamInfoEntity.setDecisionDescription(jobStartParamMstEntity.getDecisionDescription());
					m_log.debug("getTargetJobType = " + jobStartParamMstEntity.getId().getTargetJobType());
					m_log.debug("getStartDecisionValue01 = " + jobStartParamMstEntity.getId().getStartDecisionValue01());
					m_log.debug("getStartDecisionCondition = " + jobStartParamMstEntity.getId().getStartDecisionCondition());
					m_log.debug("getStartDecisionValue02 = " + jobStartParamMstEntity.getId().getStartDecisionValue02());
					m_log.debug("getDecisionDescription = " + jobStartParamMstEntity.getDecisionDescription());
					// 重複チェック
					jtm.checkEntityExists(JobStartParamInfoEntity.class, jobStartParamInfoEntity.getId());
				}
			}
		} else {
			jobInfoEntity.setSuspend(false);
			jobInfoEntity.setSkip(false);
			jobInfoEntity.setCalendar(false);
			jobInfoEntity.setStartDelay(false);
			jobInfoEntity.setEndDelay(false);
		}

		// JobParamInfoEntityを作成
		// ジョブ変数は最上位のジョブのみ設定する。
		if (first) {
			// ジョブ情報のジョブ変数を設定
			JobMstEntity jobunit = QueryUtil.getJobMstPK(jobunitId, jobunitId);
			List<JobParamMstEntity> jobParamMstEntityList = jobunit.getJobParamMstEntities();
			if (jobParamMstEntityList != null) {
				for(JobParamMstEntity jobParamMstEntity : jobParamMstEntityList) {
					// インスタンス生成
					JobParamInfoEntity jobParamInfoEntity
					= new JobParamInfoEntity(
							jobInfoEntity,
							jobParamMstEntity.getId().getParamId());
					// 重複チェック
					jtm.checkEntityExists(JobParamInfoEntity.class, jobParamInfoEntity.getId());
					jobParamInfoEntity.setParamType(jobParamMstEntity.getParamType());
					jobParamInfoEntity.setDescription(jobParamMstEntity.getDescription());
					//パラメータ値を取得
					String value = null;
					if(jobParamMstEntity.getParamType() == JobParamTypeConstant.TYPE_USER) {
						//ユーザ変数
						value = jobParamMstEntity.getValue();
					}
					jobParamInfoEntity.setValue(value);
				}
			}
			// 監視管理の情報を設定
			for (Map.Entry<String, String> entry : ParameterUtil.createParamInfo(info).entrySet()) {
				JobParamInfoEntity jobParamInfoEntity 
					= new JobParamInfoEntity(jobInfoEntity, entry.getKey());
				jobParamInfoEntity.setValue(entry.getValue());
				jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_SYSTEM_JOB);
			}
			// ジョブ契機（ファイルチェック）情報を設定
			for (Map.Entry<String, String> entry : ParameterUtil.createParamInfo(triggerInfo).entrySet()) {
				JobParamInfoEntity jobParamInfoEntity 
					= new JobParamInfoEntity(jobInfoEntity, entry.getKey());
				jobParamInfoEntity.setValue(entry.getValue());
				jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_SYSTEM_JOB);
			}
			// ジョブ契機（ジョブスケジュール、ファイルチェック）ランタイムジョブ変数デフォルト情報を設定
			if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_SCHEDULE
					|| triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_FILECHECK) {
				JobKickEntity jobKickEntity = em.find(JobKickEntity.class, triggerInfo.getJobkickId(), ObjectPrivilegeMode.NONE);
				if (jobKickEntity.getJobRuntimeParamEntities() != null) {
					for (JobRuntimeParamEntity jobRuntimeParamEntity : jobKickEntity.getJobRuntimeParamEntities()) {
						JobParamInfoEntityPK jobParamInfoEntityPK
						= new JobParamInfoEntityPK(jobInfoEntity.getId().getSessionId(),
								jobInfoEntity.getId().getJobunitId(),
								jobInfoEntity.getId().getJobId(),
								jobRuntimeParamEntity.getId().getParamId());
						JobParamInfoEntity jobParamInfoEntity
						= em.find(JobParamInfoEntity.class, jobParamInfoEntityPK, ObjectPrivilegeMode.READ);
						if (jobParamInfoEntity == null) {
							jobParamInfoEntity = new JobParamInfoEntity(jobInfoEntity, jobRuntimeParamEntity.getId().getParamId());
						}
						jobParamInfoEntity.setValue(jobRuntimeParamEntity.getDefaultValue());
						jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_RUNTIME);
					}
				}
			}
			// ランタイムジョブ変数を設定
			if (triggerInfo.getJobRuntimeParamList() != null) {
				for (JobRuntimeParam jobRuntimeParam : triggerInfo.getJobRuntimeParamList()) {
					JobParamInfoEntityPK jobParamInfoEntityPK
						= new JobParamInfoEntityPK(jobInfoEntity.getId().getSessionId(),
								jobInfoEntity.getId().getJobunitId(),
								jobInfoEntity.getId().getJobId(),
								jobRuntimeParam.getParamId());
					JobParamInfoEntity jobParamInfoEntity
						= em.find(JobParamInfoEntity.class, jobParamInfoEntityPK, ObjectPrivilegeMode.READ);
					if (jobParamInfoEntity == null) {
						jobParamInfoEntity = new JobParamInfoEntity(jobInfoEntity, jobRuntimeParam.getParamId());
					}
					jobParamInfoEntity.setValue(jobRuntimeParam.getValue());
					jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_RUNTIME);
				}
			}
		}
		//ファシリティパスを設定(JobSessionJobEntity)
		Map<String, String> jobSessionParams = null; 
		if(job.getFacilityId() != null){
			//ファシリティID取得
			String facilityId = job.getFacilityId();
			if((job.getJobType() == JobConstant.TYPE_JOB || job.getJobType() == JobConstant.TYPE_MONITORJOB)
					&& SystemParameterConstant.isParam(facilityId, SystemParameterConstant.FACILITY_ID)){
				String paramValue = ParameterUtil.getJobSessionParamValue(SystemParameterConstant.FACILITY_ID, sessionId, jobSessionParams);
				if (paramValue != null) {
					facilityId = paramValue;
				}
			}
			String scopePath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			jobSessionJobEntity.setScopeText(scopePath);
		} else if (job.getJobType() == JobConstant.TYPE_APPROVALJOB){
			
			jobSessionJobEntity.setScopeText(jobSessionJobEntity.getOwnerRoleId());
		} else {
			jobSessionJobEntity.setScopeText(null);
		}

		//JobSessionNodeEntityを作成
		if(job.getJobType() == JobConstant.TYPE_JOB
				|| job.getJobType() == JobConstant.TYPE_MONITORJOB){
			//ファシリティID取得
			String facilityId = job.getFacilityId();
			if(SystemParameterConstant.isParam(facilityId, SystemParameterConstant.FACILITY_ID)){
				String paramValue = ParameterUtil.getJobSessionParamValue(SystemParameterConstant.FACILITY_ID, sessionId, jobSessionParams);
				if (paramValue != null) {
					facilityId = paramValue;
				}
			}
			ArrayList<String> nodeIdList = new ArrayList<String>();
			if(facilityId != null){
				//ノードのファシリティIDリスト取得
				nodeIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, jobSessionJobEntity.getOwnerRoleId());
			}
			if(nodeIdList != null){
				for(String nodeId : nodeIdList){
					//ノード名を取得
					NodeInfo nodeInfo = new RepositoryControllerBean().getNode(nodeId);
					// インスタンス生成
					JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, nodeId);
					// 重複チェック
					jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
					jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
					jobSessionNodeEntity.setStartDate(null);
					jobSessionNodeEntity.setEndDate(null);
					jobSessionNodeEntity.setEndValue(null);
					jobSessionNodeEntity.setMessage(null);
					jobSessionNodeEntity.setRetryCount(0);
					jobSessionNodeEntity.setResult(null);
					jobSessionNodeEntity.setNodeName(nodeInfo.getFacilityName());
				}
			}
		}
		// 承認ジョブにおけるJobSessionNodeEntityを生成、及びジョブ変数の反映
		if(job.getJobType() == JobConstant.TYPE_APPROVALJOB){
			// ファシリティID/ファシリティ名取得(承認ジョブの場合はオーナーロールID相当)
			RoleInfo roleInfo = new AccessControllerBean().getRoleInfo(jobSessionJobEntity.getOwnerRoleId());
			JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, jobSessionJobEntity.getOwnerRoleId());
			// 重複チェック
			jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
			jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
			jobSessionNodeEntity.setStartDate(null);
			jobSessionNodeEntity.setEndDate(null);
			jobSessionNodeEntity.setEndValue(null);
			jobSessionNodeEntity.setMessage(null);
			jobSessionNodeEntity.setRetryCount(0);
			jobSessionNodeEntity.setResult(null);
			jobSessionNodeEntity.setNodeName(roleInfo.getRoleName());
			m_log.debug("getRoleName():" + roleInfo.getRoleName());
			// 実行ユーザ設定(手動実行の場合のみ設定)
			if(triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_MANUAL){
				int pre = triggerInfo.getTrigger_info().indexOf("(");
				int post = triggerInfo.getTrigger_info().length()-1;
				String userid = triggerInfo.getTrigger_info().substring(pre+1, post);
				m_log.debug("userid:" + userid);
				jobSessionNodeEntity.setApprovalRequestUser(userid);
			}
			// ジョブ変数のパラメータを置き換える
			// ジョブ変数の設定(JobParamInfoEntity作成)後、及び監視機能への通知前のタイミングとしてここで実行
			// 承認依頼文
			String reqSentence = jobInfoEntity.getApprovalReqSentence();
			reqSentence = ParameterUtil.replaceSessionParameterValue(
					sessionId,
					jobInfoEntity.getFacilityId(),
					jobInfoEntity.getApprovalReqSentence());
			// ここでリターンコードを置き換える(#[RETURN:jobid:facilityId])とnullが入るので、置き換えない。
			jobInfoEntity.setApprovalReqSentence(reqSentence);
			
			// 承認依頼メール件名
			String mailTitle = jobInfoEntity.getApprovalReqMailTitle();
			mailTitle = ParameterUtil.replaceSessionParameterValue(
					sessionId,
					jobInfoEntity.getFacilityId(),
					jobInfoEntity.getApprovalReqMailTitle());
			// ここでリターンコードを置き換える(#[RETURN:jobid:facilityId])とnullが入るので、置き換えない。
			jobInfoEntity.setApprovalReqMailTitle(mailTitle);
			
			// 承認依頼メール本文
			String mailBody = jobInfoEntity.getApprovalReqMailBody();
			mailBody = ParameterUtil.replaceSessionParameterValue(
					sessionId,
					jobInfoEntity.getFacilityId(),
					jobInfoEntity.getApprovalReqMailBody());
			// ここでリターンコードを置き換える(#[RETURN:jobid:facilityId])とnullが入るので、置き換えない。
			jobInfoEntity.setApprovalReqMailBody(mailBody);
		}

		jobInfoEntity.setBeginPriority(job.getBeginPriority());
		jobInfoEntity.setNormalPriority(job.getNormalPriority());
		jobInfoEntity.setWarnPriority(job.getWarnPriority());
		jobInfoEntity.setAbnormalPriority(job.getAbnormalPriority());
		String infoNotifyGroupId = NotifyGroupIdGenerator.generate(jobInfoEntity);
		jobInfoEntity.setNotifyGroupId(infoNotifyGroupId);

		// 終了値の設定
		// 正常
		jobInfoEntity.setNormalEndValue(job.getNormalEndValue());
		jobInfoEntity.setNormalEndValueFrom(job.getNormalEndValueFrom());
		jobInfoEntity.setNormalEndValueTo(job.getNormalEndValueTo());
		// 警告
		jobInfoEntity.setWarnEndValue(job.getWarnEndValue());
		jobInfoEntity.setWarnEndValueFrom(job.getWarnEndValueFrom());
		jobInfoEntity.setWarnEndValueTo(job.getWarnEndValueTo());
		// 異常
		jobInfoEntity.setAbnormalEndValue(job.getAbnormalEndValue());
		jobInfoEntity.setAbnormalEndValueFrom(job.getAbnormalEndValueFrom());
		jobInfoEntity.setAbnormalEndValueTo(job.getAbnormalEndValueTo());

		// 取得したマスタ情報の通知グループIDで、通知関連情報を取得する
		List<NotifyRelationInfo> ct = new NotifyControllerBean().getNotifyRelation(job.getNotifyGroupId());
		// JobNoticeInfo用の通知グループIDで、通知関連テーブルのコピーを作成する
		List<NotifyRelationInfo> infoNotifyRelationList = new ArrayList<>();
		for (NotifyRelationInfo relation : ct) {
			NotifyRelationInfo nri = new NotifyRelationInfo(infoNotifyGroupId, relation.getNotifyId());
			nri.setNotifyType(relation.getNotifyType());
			infoNotifyRelationList.add(nri);
		}
		// JobからNotifyRelationInfoは１件のみ登録すればよい。
		new NotifyControllerBean().addNotifyRelation(infoNotifyRelationList);


		//ファイル転送を行うジョブネットの実行用情報を作成
		// 通知の設定が済んでいる必要があるため、この位置で設定を行う
		if(job.getJobType() == JobConstant.TYPE_FILEJOB){
			if (CreateHulftJob.isHulftMode()) {
				new CreateHulftJob().createHulftDetailJob(jobInfoEntity);
			} else {
				// 参照ジョブの場合も通知マスタ取得のために、参照先のファイル転送ジョブのIDを引数で渡す
				new CreateFileJob().createGetFileListJob(jobInfoEntity, job.getId().getJobId());
			}
		}

		// 子供を再帰的に作成
		List<JobMstEntity> childJobMstEntities
		= em.createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class)
		.setParameter("parentJobunitId", job.getId().getJobunitId())
		.setParameter("parentJobId", job.getId().getJobId())
		.getResultList();

		if (childJobMstEntities != null) {
			for(JobMstEntity childJob : childJobMstEntities) {
				// リネームリストを渡す
				createJobSessionJob(childJob, sessionId, info, false, triggerInfo, referJob, jobIdMap);
			}
		}
		return jobSessionJobEntity.getId();
	}
	
	
	/**
	 * 参照ジョブネットの参照先ジョブネット配下のジョブIDをリネームするために
	 * リネーム前後のジョブIDのマッピング情報を作成する。
	 *
	 * @param prefix 		JobIdリネーム用プレフィックス文字列(参照ジョブネットのジョブID)
	 * @param parentJob 	チェック対象の親ジョブ
	 * @param jobIdMap 		リネーム前後のジョブIDのマッピング情報
	 * @return
	 */
	private static HashMap<String, String> getRenameJobIdMap(String prefix, JobMstEntity parentJob, HashMap<String, String> jobIdMap) {
		
		if(jobIdMap == null){
			jobIdMap = new HashMap<String, String>();
		}
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		
		List<JobMstEntity> childJobMstEntities
		= em.createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class)
		.setParameter("parentJobunitId", parentJob.getId().getJobunitId())
		.setParameter("parentJobId", parentJob.getId().getJobId())
		.getResultList();
		
		if (childJobMstEntities != null) {
			for(JobMstEntity childJob : childJobMstEntities) {
				
				int count = 0;
				StringBuilder renameJobId = new StringBuilder();
				JobMstEntity findJob = null;
				
				renameJobId.append(childJob.getId().getJobId());
				while(true){
					JobMstEntityPK key = new JobMstEntityPK(childJob.getId().getJobunitId(), renameJobId.toString());
					findJob = em.find(JobMstEntity.class, key, ObjectPrivilegeMode.READ);
					
					if(findJob == null){
						break;
					}
					
					renameJobId.setLength(0);
					if(count == 0){
						renameJobId.append(prefix).append("_");
					} else {
						renameJobId.append(prefix).append("_").append(count).append("_");
					}
					renameJobId.append(childJob.getId().getJobId());
					count++;
					
					// 最大で999とする。
					if (count == 1000) {
						break;
					}
				}
				//リネーム前後のジョブIDの対応を記録(必ず1度はリネームすることとなる)
				//key:リネーム前のジョブID、value:リネーム後のジョブID
				jobIdMap.put(childJob.getId().getJobId(), renameJobId.toString());
				m_log.debug("put for jobIdMap:" + childJob.getId().getJobId() + "->" + renameJobId.toString());
				
				jobIdMap = getRenameJobIdMap(prefix, childJob, jobIdMap);
			}
		}
		return jobIdMap;
	}
}
