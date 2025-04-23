/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.HinemosModuleConstant;
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
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.bean.EndStatusCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamRun;
import com.clustercontrol.jobmanagement.bean.JobSessionRequestMessage;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.model.JobCommandParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobCommandParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableInfoEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableMstEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobLinkInheritInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkInheritMstEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobExpInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobExpMstEntity;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderInfoEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntity;
import com.clustercontrol.jobmanagement.model.JobOutputInfoEntity;
import com.clustercontrol.jobmanagement.model.JobOutputMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaCheckEndValueInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaCheckEndValueMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaEndValueConditionInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaEndValueConditionMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaOptionInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaOptionMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaRunParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRpaRunParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobWaitGroupInfoEntity;
import com.clustercontrol.jobmanagement.model.JobWaitGroupMstEntity;
import com.clustercontrol.jobmanagement.model.JobWaitInfoEntity;
import com.clustercontrol.jobmanagement.model.JobWaitMstEntity;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.repository.bean.PlatformConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.HinemosMessage;

import jakarta.persistence.EntityExistsException;

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
	 * JobMstEntity について、子を取得するためのキャッシュ。
	 * セッション生成時に使用する想定なので、競合はない。
	 * 
	 * キー：セッションID、ジョブユニットID、親のジョブID
	 * 値：子のジョブIDのリスト
	 */
	private static Map<String, Map<String, Map<String, List<String>>>> jobMstChildrenCache = new ConcurrentHashMap<>();


	/**
	 * JobMstEntity について、子を取得するためのキャッシュ作成
	 * 
	 * @param jobunitId
	 */
	public static void makeJobMstChildrenCache(String sessionId, String jobunitId) {
		m_log.debug("makeJobMstChildrenCache(sessionId=" + sessionId + ", jobunitId=" + jobunitId + ")");

		if (jobMstChildrenCache.containsKey(sessionId)) {
			m_log.debug("makeJobMstChildrenCache(), has cache.");
			return;
		}

		// FullJob のキャッシュから、子のキャッシュを作成
		Map<String, List<String>> jobunitCache = new ConcurrentHashMap<>();
		for (JobMstEntity job : FullJob.getJobMstEntity(jobunitId)) {
			if (!job.getParentJobunitId().equals(jobunitId)) {
				m_log.debug("makeJobMstChildrenCache(), " + job.getParentJobunitId() + " is NOT jobunitId=" + jobunitId);
				continue;
			}
			String parentJobId = job.getParentJobId();
			if (!jobunitCache.containsKey(parentJobId)) {
				jobunitCache.put(parentJobId, new CopyOnWriteArrayList<>());
			}
			jobunitCache.get(parentJobId).add(job.getId().getJobId());
		}

		Map<String, Map<String, List<String>>> sessionCache = new ConcurrentHashMap<>();
		sessionCache.put(jobunitId, jobunitCache);
		jobMstChildrenCache.put(sessionId, sessionCache);
		m_log.debug("makeJobMstChildrenCache(), end. jobMstChildrenCache=" + jobMstChildrenCache);
	}

	/**
	 * JobMstEntity について、子を取得するためのキャッシュを削除
	 * 
	 * @param sessionId
	 */
	public static void removeJobMstChildrenCache(String sessionId) {
		m_log.debug("removeJobMstChildrenCache(sessionId=" + sessionId + ")");

		if (!jobMstChildrenCache.containsKey(sessionId)) {
			m_log.debug("removeJobMstChildrenCache(), NO cache");
			return;
		}
		m_log.debug("removeJobMstChildrenCache(), cache removed. sessionId=" + sessionId);
		jobMstChildrenCache.remove(sessionId);
	}

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
		String sessionId = null;
		if (triggerInfo != null 
				&& triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_SCHEDULE) {
			sessionId = CreateSessionId.getPremake(triggerInfo.getExecuteTime(), triggerInfo.getJobkickId());
		}
		if (sessionId == null) {
			sessionId = CreateSessionId.create();
		}
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
	 * @throws RpaManagementToolMasterNotFound 
	 */
	public static JobSessionJobEntityPK createJobSessionJob(
			String jobunitId, String jobId, String sessionId, OutputBasicInfo info, boolean first, JobTriggerInfo triggerInfo, JobMstEntity parentReferJob, HashMap<String, String> jobIdMap, boolean existsJobSession)
			throws JobInfoNotFound, FacilityNotFound, EntityExistsException, HinemosUnknown, JobMasterNotFound, InvalidRole, RoleNotFound, RpaManagementToolMasterNotFound {

		m_log.debug("createJobSessionJob() " + ": jobunitId=" + jobunitId + ", jobId=" + jobId + ", sessionId=" + sessionId + ", info=" + info + ", first=" + first + ", triggerInfo=" + triggerInfo + ", parentReferJob=" + parentReferJob + ", jobIdMap=" + jobIdMap + ", existsJobSession=" + existsJobSession);
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			JobSessionJobEntity jobSessionJobEntity = null;
			JobInfoEntity jobInfoEntity = null;
			JobSessionEntity jobSessionEntity = null;
			JobMstEntity referJob = null;

			if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_SCHEDULE && existsJobSession) {
				m_log.debug("createJobSessionJob(), TYPE_SCHEDULE");

				jobSessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
				jobInfoEntity = jobSessionJobEntity.getJobInfoEntity();
				jobSessionEntity = jobSessionJobEntity.getJobSessionEntity();
				// ジョブの実行状態を設定
				if (first) {
					// 最上位の保留とスキップは無視する。
					jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
				} else if(jobInfoEntity.getSuspend() != null && jobInfoEntity.getSuspend().booleanValue()) {
					//保留
					jobSessionJobEntity.setStatus(StatusConstant.TYPE_RESERVING);
				} else if(jobInfoEntity.getSkip() != null && jobInfoEntity.getSkip().booleanValue()) {
					//スキップ
					jobSessionJobEntity.setStatus(StatusConstant.TYPE_SKIP);
				} else {
					//保留・スキップ以外
					jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
				}
				// セッションノードのステータスを変更する
				List<JobSessionNodeEntity> jobSessionNodeList = jobSessionJobEntity.getJobSessionNodeEntities();
				if (jobSessionNodeList != null && jobSessionNodeList.size() > 0) {
					for (JobSessionNodeEntity jobSessionNodeEntity : jobSessionNodeList) {
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
					}
				}

				// 子供を再帰的に作成
				List<JobSessionJobEntity> childJobSessionJobEntities
				= em.createNamedQuery("JobSessionJobEntity.findChild", JobSessionJobEntity.class)
				.setParameter("sessionId", sessionId)
				.setParameter("parentJobunitId", jobunitId)
				.setParameter("parentJobId", jobId)
				.getResultList();
				if (childJobSessionJobEntities != null) {
					for(JobSessionJobEntity childJob : childJobSessionJobEntities) {
						// リネームリストを渡す
						createJobSessionJob(childJob.getId().getJobunitId(), childJob.getId().getJobId(), sessionId, info, false, triggerInfo, null, null, existsJobSession);
					}
				}
			} else {
				m_log.debug("createJobSessionJob(), not TYPE_SCHEDULE");
				
				JobMstEntity job = FullJob.getJobMstEntity(jobunitId, jobId);
				//JobSessionEntityを設定
				jobSessionEntity = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
				//親ジョブを設定
				JobSessionJobEntity parentJobSessionJobEntity = null;
				
				String parentJobunitId = job.getParentJobunitId();
				String parentJobId = job.getParentJobId();
				//親ジョブが参照ジョブネットの場合、参照先ジョブネットのジョブIDではなく
				//参照ジョブネットのジョブIDを指定する必要がある(セッションIDが参照ジョブネットのジョブIDで紐づくため)
				//ただし、孫ジョブの親には自身(子ジョブ)のジョブID(リネーム後)を設定する必要があるため、referJobをnullクリアする
				//(参照ジョブの場合は親ジョブとなることがないため、referJobがパラメタ設定されるのは、親ジョブが参照ジョブネットの場合のみ)
				if (parentReferJob != null) {
					m_log.debug("createJobSessionJob() parentReferJob jobunitId=" + parentReferJob.getId().getJobunitId() + " jobId=" + parentReferJob.getId().getJobId());
					parentJobunitId = parentReferJob.getId().getJobunitId();
					parentJobId = parentReferJob.getId().getJobId();
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
					job = FullJob.getJobMstEntity(job.getReferJobUnitId(), job.getReferJobId());
				}
				
				//JobSessionJobを作成
				//jobIdMapにリネーム情報がある場合、上位が参照ジョブネットであるためjobIdをリネームする
				//リネームリスト内のjobと同名のjobIdの場合、リネーム後のjobIdでJobSessionJobEntity,JobInfoEntityを作成する
				String referJobnetChildJobId = null;
				if(jobIdMap != null && !jobIdMap.isEmpty() && jobIdMap.get(jobId) != null){
					referJobnetChildJobId = jobId;
					jobId = jobIdMap.get(jobId);
					m_log.debug("rename jobId at JobSessionJobEntity:" + job.getId().getJobId() + "->" + jobId);
				}
			
				jobSessionJobEntity = new JobSessionJobEntity(
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
				jobSessionJobEntity.setOwnerRoleId(JobUtil.createSessioniOwnerRoleId(jobunitId));
				// 登録
				em.persist(jobSessionJobEntity);
				jobSessionJobEntity.relateToJobSessionEntity(jobSessionEntity);

				//実行状態・終了値を設定
				if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_PREMAKESESSION) {
					jobSessionJobEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
				} else {
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
				}
				//JobInfoEntityを作成
				jobInfoEntity = new JobInfoEntity(jobSessionJobEntity);
				// 重複チェック
				jtm.checkEntityExists(JobInfoEntity.class, jobInfoEntity.getId());
				// 登録
				jtm.getEntityManager().persist(jobInfoEntity);
				jobInfoEntity.relateToJobSessionJobEntity(jobSessionJobEntity);

				if(referJob != null ){
					// 参照ジョブ/ジョブネットの場合、参照元定義の名称を使う
					jobInfoEntity.setJobName(referJob.getJobName());
				}else{
					jobInfoEntity.setJobName(job.getJobName());
				}
				jobInfoEntity.setDescription(job.getDescription());
				jobInfoEntity.setJobType(job.getJobType());
				jobInfoEntity.setRegisteredModule(job.isRegisteredModule());
				jobInfoEntity.setRegDate(job.getRegDate());
				jobInfoEntity.setUpdateDate(job.getUpdateDate());
				jobInfoEntity.setRegUser(job.getRegUser());
				jobInfoEntity.setUpdateUser(job.getUpdateUser());
				jobInfoEntity.setIconId(job.getIconId());

				//待ち条件を設定
				if(job.getJobType() == JobConstant.TYPE_JOBNET ||
						job.getJobType() == JobConstant.TYPE_APPROVALJOB ||
						job.getJobType() == JobConstant.TYPE_JOB ||
						job.getJobType() == JobConstant.TYPE_FILEJOB ||
						job.getJobType() == JobConstant.TYPE_MONITORJOB ||
						job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB ||
						job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB ||
						job.getJobType() == JobConstant.TYPE_FILECHECKJOB ||
						job.getJobType() == JobConstant.TYPE_RESOURCEJOB ||
						job.getJobType() == JobConstant.TYPE_RPAJOB) {
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
					jobInfoEntity.setExclusiveBranchFlg(tmp.getExclusiveBranchFlg());
					jobInfoEntity.setExclusiveBranchEndStatus(tmp.getExclusiveBranchEndStatus());
					jobInfoEntity.setExclusiveBranchEndValue(tmp.getExclusiveBranchEndValue());
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
					jobInfoEntity.setEndDelayChangeMount(job.getEndDelayChangeMount());
					jobInfoEntity.setEndDelayChangeMountValue(job.getEndDelayChangeMountValue());
					
					jobInfoEntity.setMultiplicityNotify(job.getMultiplicityNotify());
					jobInfoEntity.setMultiplicityNotifyPriority(job.getMultiplicityNotifyPriority());
					jobInfoEntity.setMultiplicityOperation(job.getMultiplicityOperation());
					jobInfoEntity.setMultiplicityEndValue(job.getMultiplicityEndValue());

					jobInfoEntity.setJobRetryFlg(job.getJobRetryFlg());
					jobInfoEntity.setJobRetry(job.getJobRetry());
					jobInfoEntity.setJobRetryInterval(job.getJobRetryInterval());
					jobInfoEntity.setJobRetryEndStatus(job.getJobRetryEndStatus());
					
					jobInfoEntity.setQueueFlg(job.getQueueFlg());
					jobInfoEntity.setQueueId(job.getQueueId());
				}

				//実行コマンドを設定
				if(job.getJobType() == JobConstant.TYPE_JOB){
					jobInfoEntity.setFacilityId(job.getFacilityId());
					jobInfoEntity.setProcessMode(job.getProcessMode());
					if (triggerInfo.getJobCommand()) {
						jobInfoEntity.setStartCommand(triggerInfo.getJobCommandText());
					} else {
						String startCommand = job.getStartCommand();
						/**
						 * jobIdMapにリネーム情報がある場合、、上位が参照ジョブネットであるため、
						 * 起動コマンドにジョブ変数が含めていたら変数のジョブIDのところをリネームする
						 */
						if(jobIdMap != null && !jobIdMap.isEmpty()){
							startCommand = renameJobIdInVariable(startCommand, jobIdMap);
						}
						jobInfoEntity.setStartCommand(startCommand);
					}
					jobInfoEntity.setStopType(job.getStopType());
					String stopCommand = job.getStopCommand();
					/**
					 * jobIdMapにリネーム情報がある場合、、上位が参照ジョブネットであるため、
					 * 停止コマンドにジョブ変数が含めていたら変数のジョブIDのところをリネームする
					 */
					if(jobIdMap != null && !jobIdMap.isEmpty() && job.getStopType() == CommandStopTypeConstant.EXECUTE_COMMAND){
						stopCommand = renameJobIdInVariable(stopCommand, jobIdMap);
					}
					jobInfoEntity.setStopCommand(stopCommand);
					jobInfoEntity.setSpecifyUser(job.getSpecifyUser());
					jobInfoEntity.setEffectiveUser(job.getEffectiveUser());
					jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
					jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
					jobInfoEntity.setArgumentJobId(job.getArgumentJobId());
					jobInfoEntity.setArgument(job.getArgument());
					jobInfoEntity.setMessageRetry(job.getMessageRetry());
					jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
					jobInfoEntity.setCommandRetry(job.getCommandRetry());
					jobInfoEntity.setCommandRetryEndStatus(job.getCommandRetryEndStatus());
					jobInfoEntity.setJobRetryFlg(job.getJobRetryFlg());
					jobInfoEntity.setJobRetry(job.getJobRetry());
					jobInfoEntity.setJobRetryInterval(job.getJobRetryInterval());
					jobInfoEntity.setJobRetryEndStatus(job.getJobRetryEndStatus());

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
							String commandParamValue = jobCommandParamEntity.getValue();
							/**
							 * jobIdMapにリネーム情報がある場合、、上位が参照ジョブネットであるため、
							 * 停止コマンドにジョブ変数が含めていたら変数のジョブIDのところをリネームする
							 */
							if(jobIdMap != null && !jobIdMap.isEmpty()){
								commandParamValue = renameJobIdInVariable(commandParamValue, jobIdMap);
							}
							jobCommandParamInfoEntity.setValue(commandParamValue);
							// 新規登録
							em.persist(jobCommandParamInfoEntity);
							jobCommandParamInfoEntity.relateToJobCommandParamInfoEntity(jobInfoEntity);
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
							// 登録
							em.persist(jobEnvVariableInfoEntity);
							jobEnvVariableInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
					}
					
					// 標準出力のファイル出力の設定
					List<JobOutputMstEntity> jobOutputMstEntityList = job.getJobOutputMstEntities();
					if (jobOutputMstEntityList != null) {
						for(JobOutputMstEntity jobOutputMstEntity : jobOutputMstEntityList) {
							// インスタンス生成
							JobOutputInfoEntity jobOutputInfoEntity
							= new JobOutputInfoEntity(
									jobInfoEntity,
									jobOutputMstEntity.getId().getOutputType());
							jobOutputInfoEntity.setSameNormalFlg(jobOutputMstEntity.getSameNormalFlg());
							jobOutputInfoEntity.setDirectory(jobOutputMstEntity.getDirectory());
							jobOutputInfoEntity.setFileName(jobOutputMstEntity.getFileName());
							jobOutputInfoEntity.setAppendFlg(jobOutputMstEntity.getAppendFlg());
							jobOutputInfoEntity.setFailureOperationFlg(jobOutputMstEntity.getFailureOperationFlg());
							jobOutputInfoEntity.setFailureOperationType(jobOutputMstEntity.getFailureOperationType());
							jobOutputInfoEntity.setFailureOperationEndStatus(jobOutputMstEntity.getFailureOperationEndStatus());
							jobOutputInfoEntity.setFailureOperationEndValue(jobOutputMstEntity.getFailureOperationEndValue());
							jobOutputInfoEntity.setFailureNotifyFlg(jobOutputMstEntity.getFailureNotifyFlg());
							jobOutputInfoEntity.setFailureNotifyPriority(jobOutputMstEntity.getFailureNotifyPriority());
							jobOutputInfoEntity.setValid(jobOutputMstEntity.getValid());
							// 登録
							em.persist(jobOutputInfoEntity);
							jobOutputInfoEntity.relateToJobInfoEntity(jobInfoEntity);
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

				//ジョブ連携送信ジョブ情報を設定
				if(job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB){
					jobInfoEntity.setMessageRetry(job.getMessageRetry());
					jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
					jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
					jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
					jobInfoEntity.setCommandRetry(job.getCommandRetry());
					jobInfoEntity.setCommandRetryEndStatus(job.getCalendarEndStatus());
					jobInfoEntity.setRetryFlg(job.getRetryFlg());
					jobInfoEntity.setRetryCount(job.getRetryCount());
					jobInfoEntity.setFailureOperation(job.getFailureOperation());
					jobInfoEntity.setFailureEndStatus(job.getFailureEndStatus());
					jobInfoEntity.setJoblinkMessageId(job.getJoblinkMessageId());
					jobInfoEntity.setPriority(job.getPriority());
					String message = job.getMessage();
					/**
					 * jobIdMapにリネーム情報がある場合、上位が参照ジョブネットであるため
					 * messageのジョブIDのところをリネームする
					 */
					if(jobIdMap != null && !jobIdMap.isEmpty()){
						message = renameJobIdInVariable(message, jobIdMap);
					}
					jobInfoEntity.setMessage(message);
					jobInfoEntity.setSuccessEndValue(job.getSuccessEndValue());
					jobInfoEntity.setFailureEndValue(job.getFailureEndValue());
					jobInfoEntity.setJoblinkSendSettingId(job.getJoblinkSendSettingId());
					jobInfoEntity.setSpecifyUser(false);
					jobInfoEntity.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);

					
					
					
					// ジョブ連携送信設定情報取得
					JobLinkSendSettingEntity settingEntity
						= QueryUtil.getJobLinkSendSettingPK_OR(job.getJoblinkSendSettingId(), jobSessionJobEntity.getOwnerRoleId());
					jobInfoEntity.setFacilityId(settingEntity.getFacilityId());
					jobInfoEntity.setProcessMode(settingEntity.getProcessMode());
					jobInfoEntity.setProtocol(settingEntity.getProtocol());
					jobInfoEntity.setPort(settingEntity.getPort());
					jobInfoEntity.setHinemosUserId(settingEntity.getHinemosUserId());
					jobInfoEntity.setHinemosPassword(settingEntity.getHinemosPassword());
					jobInfoEntity.setProxyFlg(settingEntity.getProxyFlg());
					jobInfoEntity.setProxyHost(settingEntity.getProxyHost());
					jobInfoEntity.setProxyPort(settingEntity.getProxyPort()); 
					jobInfoEntity.setProxyUser(settingEntity.getProxyUser());
					jobInfoEntity.setProxyPassword(settingEntity.getProxyPassword());
					// ジョブ連携メッセージの拡張情報設定
					List<JobLinkJobExpMstEntity> jobLinkJobExpMstEntityList = job.getJobLinkJobExpMstEntities();
					if (jobLinkJobExpMstEntityList != null) {
						for(JobLinkJobExpMstEntity jobLinkJobExpMstEntity : jobLinkJobExpMstEntityList) {
							// インスタンス生成
							JobLinkJobExpInfoEntity jobLinkJobExpInfoEntity
								= new JobLinkJobExpInfoEntity(jobInfoEntity, jobLinkJobExpMstEntity.getId().getKey());
							// 重複チェック
							jtm.checkEntityExists(JobLinkJobExpInfoEntity.class, jobLinkJobExpInfoEntity.getId());
							String expValue = jobLinkJobExpMstEntity.getValue();
							/**
							 * jobIdMapにリネーム情報がある場合、上位が参照ジョブネットであるため
							 * messageのジョブIDのところをリネームする
							 */
							if(jobIdMap != null && !jobIdMap.isEmpty()){
								expValue = renameJobIdInVariable(expValue, jobIdMap);
							}
							jobLinkJobExpInfoEntity.setValue(expValue);
							// 登録
							em.persist(jobLinkJobExpInfoEntity);
							jobLinkJobExpInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
					}
				}

				//ジョブ連携待機ジョブ情報を設定
				if(job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB){
					jobInfoEntity.setProcessMode(job.getProcessMode());
					jobInfoEntity.setFacilityId(job.getFacilityId());
					jobInfoEntity.setMessageRetry(job.getMessageRetry());
					jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
					jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
					jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
					jobInfoEntity.setCommandRetry(job.getCommandRetry());
					jobInfoEntity.setCommandRetryEndStatus(job.getCommandRetryEndStatus());
					jobInfoEntity.setMonitorInfoEndValue(job.getMonitorInfoEndValue());
					jobInfoEntity.setMonitorWarnEndValue(job.getMonitorWarnEndValue());
					jobInfoEntity.setMonitorCriticalEndValue(job.getMonitorCriticalEndValue());
					jobInfoEntity.setMonitorUnknownEndValue(job.getMonitorUnknownEndValue());
					jobInfoEntity.setFailureEndFlg(job.getFailureEndFlg());
					jobInfoEntity.setMonitorWaitTime(job.getMonitorWaitTime());
					jobInfoEntity.setMonitorWaitEndValue(job.getMonitorWaitEndValue());
					jobInfoEntity.setJoblinkMessageId(job.getJoblinkMessageId());
					jobInfoEntity.setMessage(job.getMessage());
					jobInfoEntity.setPastFlg(job.getPastFlg());
					jobInfoEntity.setPastMin(job.getPastMin());
					jobInfoEntity.setInfoValidFlg(job.getInfoValidFlg());
					jobInfoEntity.setWarnValidFlg(job.getWarnValidFlg());
					jobInfoEntity.setCriticalValidFlg(job.getCriticalValidFlg());
					jobInfoEntity.setUnknownValidFlg(job.getUnknownValidFlg());
					jobInfoEntity.setApplicationFlg(job.getApplicationFlg());
					jobInfoEntity.setApplication(job.getApplication());
					jobInfoEntity.setMonitorDetailIdFlg(job.getMonitorDetailIdFlg());
					jobInfoEntity.setMonitorDetailId(job.getMonitorDetailId());
					jobInfoEntity.setMessageFlg(job.getMessageFlg());
					jobInfoEntity.setExpFlg(job.getExpFlg());
					jobInfoEntity.setMonitorAllEndValueFlg(job.getMonitorAllEndValueFlg());
					jobInfoEntity.setMonitorAllEndValue(job.getMonitorAllEndValue());
					jobInfoEntity.setSpecifyUser(false);
					jobInfoEntity.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);

					// ジョブ連携メッセージの拡張情報設定
					List<JobLinkJobExpMstEntity> jobLinkJobExpMstEntityList = job.getJobLinkJobExpMstEntities();
					if (jobLinkJobExpMstEntityList != null) {
						for(JobLinkJobExpMstEntity jobLinkJobExpMstEntity : jobLinkJobExpMstEntityList) {
							// インスタンス生成
							JobLinkJobExpInfoEntity jobLinkJobExpInfoEntity
								= new JobLinkJobExpInfoEntity(jobInfoEntity, jobLinkJobExpMstEntity.getId().getKey());
							// 重複チェック
							jtm.checkEntityExists(JobLinkJobExpInfoEntity.class, jobLinkJobExpInfoEntity.getId());
							jobLinkJobExpInfoEntity.setValue(jobLinkJobExpMstEntity.getValue());
							// 登録
							em.persist(jobLinkJobExpInfoEntity);
							jobLinkJobExpInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
					}

					// メッセージの引継ぎ情報設定
					List<JobLinkInheritMstEntity> jobLinkInheritMstEntityList = job.getJobLinkInheritMstEntities();
					if (jobLinkInheritMstEntityList != null) {
						for(JobLinkInheritMstEntity jobLinkInheritMstEntity : jobLinkInheritMstEntityList) {
							// インスタンス生成
							JobLinkInheritInfoEntity jobLinkInheritInfoEntity
								= new JobLinkInheritInfoEntity(jobInfoEntity, jobLinkInheritMstEntity.getId().getParamId());
							// 重複チェック
							jtm.checkEntityExists(JobLinkInheritInfoEntity.class, jobLinkInheritInfoEntity.getId());
							jobLinkInheritInfoEntity.setKeyInfo(jobLinkInheritMstEntity.getKeyInfo());
							jobLinkInheritInfoEntity.setExpKey(jobLinkInheritMstEntity.getExpKey());
							// 登録
							em.persist(jobLinkInheritInfoEntity);
							jobLinkInheritInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
					}
				}

				//ファイルチェックジョブ情報を設定
				if(job.getJobType() == JobConstant.TYPE_FILECHECKJOB){
					jobInfoEntity.setFacilityId(job.getFacilityId());
					jobInfoEntity.setProcessMode(job.getProcessMode());
					jobInfoEntity.setSuccessEndValue(job.getSuccessEndValue());
					jobInfoEntity.setFailureEndFlg(job.getFailureEndFlg());
					jobInfoEntity.setFailureWaitTime(job.getFailureWaitTime());
					jobInfoEntity.setFailureEndValue(job.getFailureEndValue());
					jobInfoEntity.setDirectory(job.getDirectory());
					jobInfoEntity.setFileName(job.getFileName());
					jobInfoEntity.setCreateValidFlg(job.getCreateValidFlg());
					jobInfoEntity.setCreateBeforeJobStartFlg(job.getCreateBeforeJobStartFlg());
					jobInfoEntity.setDeleteValidFlg(job.getDeleteValidFlg());
					jobInfoEntity.setModifyValidFlg(job.getModifyValidFlg());
					jobInfoEntity.setModifyType(job.getModifyType());
					jobInfoEntity.setNotJudgeFileInUseFlg(job.getNotJudgeFileInUseFlg());
				}
				
				//RPAシナリオジョブ情報を設定
				if(job.getJobType() == JobConstant.TYPE_RPAJOB){
					jobInfoEntity.setFacilityId(job.getFacilityId());
					jobInfoEntity.setProcessMode(job.getProcessMode());
					jobInfoEntity.setStartCommand(job.getStartCommand());
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
					jobInfoEntity.setCommandRetryEndStatus(job.getCommandRetryEndStatus());
					jobInfoEntity.setJobRetryFlg(job.getJobRetryFlg());
					jobInfoEntity.setJobRetry(job.getJobRetry());
					jobInfoEntity.setJobRetryEndStatus(job.getJobRetryEndStatus());
					jobInfoEntity.setRpaJobType(job.getRpaJobType());
					jobInfoEntity.setRpaToolId(job.getRpaToolId());
					jobInfoEntity.setRpaExeFilepath(job.getRpaExeFilepath());
					jobInfoEntity.setRpaScenarioFilepath(job.getRpaScenarioFilepath());
					jobInfoEntity.setRpaLogDirectory(job.getRpaLogDirectory());
					jobInfoEntity.setRpaLogFileName(job.getRpaLogFileName());
					jobInfoEntity.setRpaLogEncoding(job.getRpaLogEncoding());
					jobInfoEntity.setRpaLogReturnCode(job.getRpaLogReturnCode());
					jobInfoEntity.setRpaLogPatternHead(job.getRpaLogPatternHead());
					jobInfoEntity.setRpaLogPatternTail(job.getRpaLogPatternTail());
					jobInfoEntity.setRpaLogMaxBytes(job.getRpaLogMaxBytes());
					jobInfoEntity.setRpaDefaultEndValue(job.getRpaDefaultEndValue());
					jobInfoEntity.setRpaLoginFlg(job.getRpaLoginFlg());
					jobInfoEntity.setRpaLoginUserId(job.getRpaLoginUserId());
					jobInfoEntity.setRpaLoginPassword(job.getRpaLoginPassword());
					jobInfoEntity.setRpaLoginRetry(job.getRpaLoginRetry());
					jobInfoEntity.setRpaLoginEndValue(job.getRpaLoginEndValue());
					jobInfoEntity.setRpaLoginResolution(job.getRpaLoginResolution());
					jobInfoEntity.setRpaLogoutFlg(job.getRpaLogoutFlg());
					jobInfoEntity.setRpaScreenshotEndDelayFlg(job.getRpaScreenshotEndDelayFlg());
					jobInfoEntity.setRpaScreenshotEndValueFlg(job.getRpaScreenshotEndValueFlg());
					jobInfoEntity.setRpaScreenshotEndValue(job.getRpaScreenshotEndValue());
					jobInfoEntity.setRpaScreenshotEndValueCondition(job.getRpaScreenshotEndValueCondition());
					jobInfoEntity.setRpaNotLoginNotify(job.getRpaNotLoginNotify());
					jobInfoEntity.setRpaNotLoginNotifyPriority(job.getRpaNotLoginNotifyPriority());
					jobInfoEntity.setRpaNotLoginEndValue(job.getRpaNotLoginEndValue());
					jobInfoEntity.setRpaAlreadyRunningNotify(job.getRpaAlreadyRunningNotify());
					jobInfoEntity.setRpaAlreadyRunningNotifyPriority(job.getRpaAlreadyRunningNotifyPriority());
					jobInfoEntity.setRpaAlreadyRunningEndValue(job.getRpaAlreadyRunningEndValue());
					jobInfoEntity.setRpaAbnormalExitNotify(job.getRpaAbnormalExitNotify());
					jobInfoEntity.setRpaAbnormalExitNotifyPriority(job.getRpaAbnormalExitNotifyPriority());
					jobInfoEntity.setRpaAbnormalExitEndValue(job.getRpaAbnormalExitEndValue());
					jobInfoEntity.setRpaScopeId(job.getRpaScopeId());
					jobInfoEntity.setRpaRunType(job.getRpaRunType());
					jobInfoEntity.setRpaScenarioParam(job.getRpaScenarioParam());
					jobInfoEntity.setRpaStopType(job.getRpaStopType());
					jobInfoEntity.setRpaStopMode(job.getRpaStopMode());
					jobInfoEntity.setRpaRunConnectTimeout(job.getRpaRunConnectTimeout());
					jobInfoEntity.setRpaRunRequestTimeout(job.getRpaRunRequestTimeout());
					jobInfoEntity.setRpaRunEndFlg(job.getRpaRunEndFlg());
					jobInfoEntity.setRpaRunRetry(job.getRpaRunRetry());
					jobInfoEntity.setRpaRunEndValue(job.getRpaRunEndValue());
					jobInfoEntity.setRpaCheckConnectTimeout(job.getRpaCheckConnectTimeout());
					jobInfoEntity.setRpaCheckRequestTimeout(job.getRpaCheckRequestTimeout());
					jobInfoEntity.setRpaCheckEndFlg(job.getRpaCheckEndFlg());
					jobInfoEntity.setRpaCheckRetry(job.getRpaCheckRetry());
					jobInfoEntity.setRpaCheckEndValue(job.getRpaCheckEndValue());
					m_log.debug("Rpa: jobType = " + job.getRpaJobType());
					m_log.debug("Rpa: rpaToolId = " + job.getRpaToolId());
					m_log.debug("Rpa: exeFilepath = " + job.getRpaExeFilepath());
					m_log.debug("Rpa: scenarioFilepath = " + job.getRpaScenarioFilepath());
					m_log.debug("Rpa: logDirectory = " + job.getRpaLogDirectory());
					m_log.debug("Rpa: logFileName = " + job.getRpaLogFileName());
					m_log.debug("Rpa: logFileEncoding = " + job.getRpaLogEncoding());
					m_log.debug("Rpa: logFileReturnCode = " + job.getRpaLogReturnCode());
					m_log.debug("Rpa: logPatternHead = " + job.getRpaLogPatternHead());
					m_log.debug("Rpa: logPatternTail = " + job.getRpaLogPatternTail());
					m_log.debug("Rpa: logMaxBytes = " + job.getRpaLogMaxBytes());
					m_log.debug("Rpa: defaultEndValue = " + job.getRpaDefaultEndValue());
					m_log.debug("Rpa: loginFlg = " + job.getRpaLoginFlg());
					m_log.debug("Rpa: loginUserId = " + job.getRpaLoginUserId());
					m_log.debug("Rpa: loginRetry = " + job.getRpaLoginRetry());
					m_log.debug("Rpa: loginEndValue = " + job.getRpaLoginEndValue());
					m_log.debug("Rpa: loginResolution = " + job.getRpaLoginResolution());
					m_log.debug("Rpa: logoutFlg = " + job.getRpaLogoutFlg());
					m_log.debug("Rpa: notLoginNotify = " + job.getRpaNotLoginNotify());
					m_log.debug("Rpa: notLoginNotifyPriority = " + job.getRpaNotLoginNotifyPriority());
					m_log.debug("Rpa: notLoginEndValue = " + job.getRpaNotLoginEndValue());
					m_log.debug("Rpa: alreadyRunningNotify = " + job.getRpaAlreadyRunningNotify());
					m_log.debug("Rpa: alreadyRunningNotifyPriority = " + job.getRpaAlreadyRunningNotifyPriority());
					m_log.debug("Rpa: alreadyRunningEndValue = " + job.getRpaAlreadyRunningEndValue());
					m_log.debug("Rpa: abnormalExitNotify = " + job.getRpaAbnormalExitNotify());
					m_log.debug("Rpa: abnormalExitNotifyPriority = " + job.getRpaAbnormalExitNotifyPriority());
					m_log.debug("Rpa: abnormalExitEndValue = " + job.getRpaAbnormalExitEndValue());
					m_log.debug("Rpa: screenshotEndDelayFlg = " + job.getRpaScreenshotEndDelayFlg());
					m_log.debug("Rpa: screenshotEndValueFlg = " + job.getRpaScreenshotEndValueFlg());
					m_log.debug("Rpa: screenshotEndValue = " + job.getRpaScreenshotEndValue());
					m_log.debug("Rpa: screenshotEndValueCondition = " + job.getRpaScreenshotEndValueCondition());
					m_log.debug("Rpa: rpaScopeId = " + job.getRpaScopeId());
					m_log.debug("Rpa: rpaRunType = " + job.getRpaRunType());
					m_log.debug("Rpa: rpaScenarioParam = " + job.getRpaScenarioParam());
					m_log.debug("Rpa: rpaStopType = " + job.getRpaStopType());
					m_log.debug("Rpa: rpaStopMode = " + job.getRpaStopMode());
					m_log.debug("Rpa: rpaRunConnectTimeout = " + job.getRpaRunConnectTimeout());
					m_log.debug("Rpa: rpaRunRequestTimeout = " + job.getRpaRunRequestTimeout());
					m_log.debug("Rpa: rpaRunEndFlg = " + job.getRpaRunEndFlg());
					m_log.debug("Rpa: rpaRunRetry = " + job.getRpaRunRetry());
					m_log.debug("Rpa: rpaRunEndValue = " + job.getRpaRunEndValue());
					m_log.debug("Rpa: rpaCheckConnectTimeout = " + job.getRpaCheckConnectTimeout());
					m_log.debug("Rpa: rpaCheckRequestTimeout = " + job.getRpaCheckRequestTimeout());
					m_log.debug("Rpa: rpaCheckEndFlg = " + job.getRpaCheckEndFlg());
					m_log.debug("Rpa: rpaCheckRetry = " + job.getRpaCheckRetry());
					m_log.debug("Rpa: rpaCheckEndValue = " + job.getRpaCheckEndValue());
					//JobRpaOptionInfoEntityを作成
					List<JobRpaOptionMstEntity> jobRpaOptionMstEntityList = job.getJobRpaOptionMstEntities();
					if (jobRpaOptionMstEntityList != null) {
						for(JobRpaOptionMstEntity jobRpaOptionMstEntity : jobRpaOptionMstEntityList) {
							JobRpaOptionInfoEntity jobRpaOptionInfoEntity = new JobRpaOptionInfoEntity(jobInfoEntity, jobRpaOptionMstEntity.getId().getOrderNo());
							jobRpaOptionInfoEntity.setOption(jobRpaOptionMstEntity.getOption());
							jobRpaOptionInfoEntity.setDescription(jobRpaOptionMstEntity.getDescription());
							m_log.debug("RpaOption: order = " + jobRpaOptionMstEntity.getId().getOrderNo());
							m_log.debug("RpaOption: option = " + jobRpaOptionMstEntity.getOption());
							m_log.debug("RpaOption: description= " + jobRpaOptionMstEntity.getDescription());
							jobRpaOptionInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
					}
					//JobRpaEndValueConditionInfoEntityを作成
					List<JobRpaEndValueConditionMstEntity> jobRpaEndValueConditionMstEntityList = job.getJobRpaEndValueConditionMstEntities();
					if (jobRpaEndValueConditionMstEntityList != null) {
						for(JobRpaEndValueConditionMstEntity jobRpaEndValueConditionMstEntity : jobRpaEndValueConditionMstEntityList) {
							JobRpaEndValueConditionInfoEntity jobRpaEndValueConditionInfoEntity = new JobRpaEndValueConditionInfoEntity(jobInfoEntity,
									jobRpaEndValueConditionMstEntity.getId().getOrderNo());
							jobRpaEndValueConditionInfoEntity.setConditionType(jobRpaEndValueConditionMstEntity.getConditionType());
							jobRpaEndValueConditionInfoEntity.setPattern(jobRpaEndValueConditionMstEntity.getPattern());
							jobRpaEndValueConditionInfoEntity.setCaseSensitivityFlg(jobRpaEndValueConditionMstEntity.getCaseSensitivityFlg());
							jobRpaEndValueConditionInfoEntity.setProcessType(jobRpaEndValueConditionMstEntity.getProcessType());
							jobRpaEndValueConditionInfoEntity.setReturnCode(jobRpaEndValueConditionMstEntity.getReturnCode());
							jobRpaEndValueConditionInfoEntity.setReturnCodeCondition(jobRpaEndValueConditionMstEntity.getReturnCodeCondition());
							jobRpaEndValueConditionInfoEntity.setUseCommandReturnCodeFlg(jobRpaEndValueConditionMstEntity.getUseCommandReturnCodeFlg());
							jobRpaEndValueConditionInfoEntity.setEndValue(jobRpaEndValueConditionMstEntity.getEndValue());
							jobRpaEndValueConditionInfoEntity.setDescription(jobRpaEndValueConditionMstEntity.getDescription());
							m_log.debug("RpaEndValueCondition: order= " + jobRpaEndValueConditionInfoEntity.getId().getOrderNo());
							m_log.debug("RpaEndValueCondition: conditionType= " + jobRpaEndValueConditionInfoEntity.getConditionType());
							m_log.debug("RpaEndValueCondition: pattern= " + jobRpaEndValueConditionInfoEntity.getPattern());
							m_log.debug("RpaEndValueCondition: caseSensitivityFlg= " + jobRpaEndValueConditionInfoEntity.getCaseSensitivityFlg());
							m_log.debug("RpaEndValueCondition: processType= " + jobRpaEndValueConditionInfoEntity.getProcessType());
							m_log.debug("RpaEndValueCondition: returnCode= " + jobRpaEndValueConditionInfoEntity.getReturnCode());
							m_log.debug("RpaEndValueCondition: returnCodeCondition= " + jobRpaEndValueConditionInfoEntity.getReturnCodeCondition());
							m_log.debug("RpaEndValueCondition: useCommandReturnCodeFlg= " + jobRpaEndValueConditionInfoEntity.getUseCommandReturnCodeFlg());
							m_log.debug("RpaEndValueCondition: endValue= " + jobRpaEndValueConditionInfoEntity.getEndValue());
							m_log.debug("RpaEndValueCondition: description= " + jobRpaEndValueConditionInfoEntity.getDescription());
							jobRpaEndValueConditionInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
					}
					//JobRpaRunParamInfoEntityを作成
					List<JobRpaRunParamMstEntity> jobRpaRunParamMstEntityList = job.getJobRpaRunParamMstEntities();
					if (jobRpaRunParamMstEntityList != null) {
						for (JobRpaRunParamMstEntity jobRpaRunParamMstEntity : jobRpaRunParamMstEntityList) {
							JobRpaRunParamInfoEntity jobRpaRunParamInfoEntity = new JobRpaRunParamInfoEntity(jobInfoEntity,
									jobRpaRunParamMstEntity.getId().getParamId());
							jobRpaRunParamInfoEntity.setParamName(jobRpaRunParamMstEntity.getParamName());
							jobRpaRunParamInfoEntity.setParamValue(jobRpaRunParamMstEntity.getParamValue());
							jobRpaRunParamInfoEntity.setParamType(jobRpaRunParamMstEntity.getParamType());
							jobRpaRunParamInfoEntity.setArrayFlg(jobRpaRunParamMstEntity.getArrayFlg());
							m_log.debug("RpaJobRunParamInfo: paramId=" + jobRpaRunParamInfoEntity.getId().getParamId());
							m_log.debug("RpaJobRunParamInfo: paramName=" + jobRpaRunParamInfoEntity.getParamName());
							m_log.debug("RpaJobRunParamInfo: paramValue=" + jobRpaRunParamInfoEntity.getParamValue());
							m_log.debug("RpaJobRunParamInfo: paramType=" + jobRpaRunParamInfoEntity.getParamType());
							m_log.debug("RpaJobRunParamInfo: arrayFlg=" + jobRpaRunParamInfoEntity.getArrayFlg());
							jobRpaRunParamInfoEntity.relateToJobInfoEntity(jobInfoEntity);
							jobRpaRunParamInfoEntity.setRpaManagementToolRunParamMst(
									jobRpaRunParamMstEntity.getRpaManagementToolRunParamMst());
						}
					}
					//JobRpaCheckEndValueInfoEntityを作成
					List<JobRpaCheckEndValueMstEntity> jobRpaCheckEndValueMstEntityList = job.getJobRpaCheckEndValueMstEntities();
					if (jobRpaCheckEndValueMstEntityList != null) {
						for (JobRpaCheckEndValueMstEntity jobRpaCheckEndValueMstEntity : jobRpaCheckEndValueMstEntityList) {
							JobRpaCheckEndValueInfoEntity jobRpaCheckEndValueInfoEntity = new JobRpaCheckEndValueInfoEntity(jobInfoEntity,
									jobRpaCheckEndValueMstEntity.getId().getEndStatusId());
							jobRpaCheckEndValueInfoEntity.setEndStatus(jobRpaCheckEndValueMstEntity.getEndStatus());
							jobRpaCheckEndValueInfoEntity.setEndValue(jobRpaCheckEndValueMstEntity.getEndValue());
							m_log.debug("RpaJobCheckEndValueInfo: endStatusId=" + jobRpaCheckEndValueInfoEntity.getId().getEndStatusId());
							m_log.debug("RpaJobCheckEndValueInfo: endStatus=" + jobRpaCheckEndValueInfoEntity.getEndStatus());
							m_log.debug("RpaJobCheckEndValueInfo: endValue=" + jobRpaCheckEndValueInfoEntity.getEndValue());
							jobRpaCheckEndValueInfoEntity.relateToJobInfoEntity(jobInfoEntity);
							jobRpaCheckEndValueInfoEntity.setRpaManagementToolEndStatusMst(
									jobRpaCheckEndValueMstEntity.getRpaManagementToolEndStatusMst());
						}
					}
				}

				//ファイルチェックジョブ情報を設定
				if(job.getJobType() == JobConstant.TYPE_FILECHECKJOB){
					jobInfoEntity.setFacilityId(job.getFacilityId());
					jobInfoEntity.setProcessMode(job.getProcessMode());
					jobInfoEntity.setSuccessEndValue(job.getSuccessEndValue());
					jobInfoEntity.setFailureEndFlg(job.getFailureEndFlg());
					jobInfoEntity.setFailureWaitTime(job.getFailureWaitTime());
					jobInfoEntity.setFailureEndValue(job.getFailureEndValue());
					jobInfoEntity.setDirectory(job.getDirectory());
					jobInfoEntity.setFileName(job.getFileName());
					jobInfoEntity.setCreateValidFlg(job.getCreateValidFlg());
					jobInfoEntity.setCreateBeforeJobStartFlg(job.getCreateBeforeJobStartFlg());
					jobInfoEntity.setDeleteValidFlg(job.getDeleteValidFlg());
					jobInfoEntity.setModifyValidFlg(job.getModifyValidFlg());
					jobInfoEntity.setModifyType(job.getModifyType());
					jobInfoEntity.setNotJudgeFileInUseFlg(job.getNotJudgeFileInUseFlg());
					jobInfoEntity.setStopType(job.getStopType());
					jobInfoEntity.setMessageRetry(job.getMessageRetry());
					jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
					jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
					// コマンドの繰り返し実行は行わないがエラー回避のため設定はしておく（ジョブ作成時に必ずfalseになる）
					jobInfoEntity.setCommandRetryFlg(job.getCommandRetryFlg());
					jobInfoEntity.setCommandRetry(job.getCommandRetry());
					jobInfoEntity.setCommandRetryEndStatus(job.getCommandRetryEndStatus());
				}

				// リソース制御ジョブ情報を設定
				if (job.getJobType() == JobConstant.TYPE_RESOURCEJOB) {
					// 通知先（facilityIdに入っている）
					jobInfoEntity.setFacilityId(job.getFacilityId());

					// リソース制御ジョブ情報をセット
					jobInfoEntity.setResourceCloudScopeId(job.getResourceCloudScopeId());
					jobInfoEntity.setResourceLocationId(job.getResourceLocationId());
					jobInfoEntity.setResourceType(job.getResourceType());
					jobInfoEntity.setResourceAction(job.getResourceAction());
					jobInfoEntity.setResourceTargetId(job.getResourceTargetId());
					jobInfoEntity.setResourceStatusConfirmTime(job.getResourceStatusConfirmTime());
					jobInfoEntity.setResourceStatusConfirmInterval(job.getResourceStatusConfirmInterval());
					jobInfoEntity.setResourceAttachNode(job.getResourceAttachNode());
					jobInfoEntity.setResourceAttachDevice(job.getResourceAttachDevice());
					jobInfoEntity.setResourceSuccessValue(job.getResourceSuccessValue());
					jobInfoEntity.setResourceFailureValue(job.getResourceFailureValue());

					// ノード処理で必要な値をセット（以前コマンドジョブ扱いだったとき、共通で入れていた値をセット）
					jobInfoEntity.setMessageRetryEndFlg(false);
					jobInfoEntity.setMessageRetryEndValue(-1);
					jobInfoEntity.setCommandRetry(10);
					jobInfoEntity.setCommandRetryFlg(false);
					jobInfoEntity.setMessageRetry(10);
					jobInfoEntity.setProcessMode(ProcessingMethodConstant.TYPE_ALL_NODE);
					jobInfoEntity.setSpecifyUser(false);
					jobInfoEntity.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
					jobInfoEntity.setArgumentJobId(job.getArgumentJobId());
					jobInfoEntity.setArgument(job.getArgument());
					jobInfoEntity.setMessageRetry(job.getMessageRetry());
					jobInfoEntity.setMessageRetryEndFlg(job.getMessageRetryEndFlg());
					jobInfoEntity.setMessageRetryEndValue(job.getMessageRetryEndValue());
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

					//JobWaitGroupInfoEntity、JobWaitInfoEntityを作成
					Boolean waitCheckFlg = false;
					List<JobWaitGroupMstEntity> jobWaitGroupMstEntityList = tmp.getJobWaitGroupMstEntities();
					if (jobWaitGroupMstEntityList != null) {
						int orderNo = 0;
						for(JobWaitGroupMstEntity jobWaitGroupMstEntity : jobWaitGroupMstEntityList) {
							if (jobWaitGroupMstEntity.getJobWaitMstEntities() == null) {
								continue;
							}
							JobWaitGroupInfoEntity jobWaitGroupInfoEntity = new JobWaitGroupInfoEntity(jobInfoEntity, orderNo);
							jobWaitGroupInfoEntity.setConditionType(jobWaitGroupMstEntity.getConditionType());
							jobWaitGroupInfoEntity.setJobWaitInfoEntities(new ArrayList<>());
							// 重複チェック
							jtm.checkEntityExists(JobWaitGroupInfoEntity.class, jobWaitGroupInfoEntity.getId());
							for (JobWaitMstEntity jobWaitMstEntity : jobWaitGroupMstEntity.getJobWaitMstEntities()) {

								// 待ち条件－時刻を、ジョブ手動起動時のダイアログで「無視する」を有効にした場合
								if (jobWaitMstEntity.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_TIME
									&& triggerInfo.getJobWaitTime()) {
									continue;
								}
								if (jobWaitMstEntity.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_TIME) {
									jobInfoEntity.addWaitRuleTime(jobWaitMstEntity.getId().getTargetLong());
								}

								// 待ち条件－セッション開始時の時間（分）を、ジョブ手動起動時のダイアログで「無視する」を有効にした場合
								if (jobWaitMstEntity.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_START_MINUTE
									&& triggerInfo.getJobWaitMinute()) {
									continue;
								}

								//jobIdMapにリネーム情報がある場合、上位が参照ジョブネットであるため待ち条件内のjobIdをリネームする
								//待ち条件に指定するjobIdが、リネームリスト内のjobと同名のjobIdの場合、リネーム後のjobIdでJobWaitInfoEntityを作成する
								String targetJobId = null;
								if(jobIdMap != null && !jobIdMap.isEmpty()
										&& jobIdMap.containsKey(jobWaitMstEntity.getId().getTargetJobId())){
									targetJobId = jobIdMap.get(jobWaitMstEntity.getId().getTargetJobId());
									m_log.debug("rename jobId at jobWaitMstEntity:" + jobWaitMstEntity.getId().getTargetJobId() + "->" + targetJobId);
								} else {
									targetJobId = jobWaitMstEntity.getId().getTargetJobId();
								}

								String targetValue01 = jobWaitMstEntity.getId().getTargetStr1();
								String targetValue02 = jobWaitMstEntity.getId().getTargetStr2();
								/**
								 * jobIdMapにリネーム情報がある場合、上位が参照ジョブネットであるため
								 * targetValue01とtargetValue02のジョブIDのところをリネームする
								 */
								if(jobIdMap != null && !jobIdMap.isEmpty()){
									targetValue01 = renameJobIdInVariable(targetValue01, jobIdMap);
									targetValue02 = renameJobIdInVariable(targetValue02, jobIdMap);
								}

								JobWaitInfoEntity jobWaitInfoEntity
									= new JobWaitInfoEntity(jobWaitGroupInfoEntity,
											jobWaitMstEntity.getId().getTargetJobType(),
											jobWaitMstEntity.getId().getJobunitId(),
											targetJobId,
											jobWaitMstEntity.getId().getTargetInt1(),
											jobWaitMstEntity.getId().getTargetInt2(),
											targetValue01,
											targetValue02,
											jobWaitMstEntity.getId().getTargetLong());
								jobWaitInfoEntity.setDescription(jobWaitMstEntity.getDescription());
								// 重複チェック
								jtm.checkEntityExists(JobWaitInfoEntity.class, jobWaitInfoEntity.getId());
								jobWaitInfoEntity.relateToJobWaitGroupInfoEntity(jobWaitGroupInfoEntity);

									//ジョブ変数待ち条件、セッション横断ジョブ待ち条件の場合フラグをtrueにする
								if (jobWaitInfoEntity.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS 
									|| jobWaitInfoEntity.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE
									|| jobWaitInfoEntity.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
									waitCheckFlg = true;
								}
							}
							if (jobWaitGroupInfoEntity.getJobWaitInfoEntities().isEmpty()) {
								continue;
							}
							jobWaitGroupInfoEntity.setIsGroup(jobWaitGroupInfoEntity.getJobWaitInfoEntities().size() > 1);
							jobWaitGroupInfoEntity.relateToJobInfoEntity(jobInfoEntity);
							// 登録
							em.persist(jobWaitGroupInfoEntity);
							orderNo++;
						}
					}
					//ジョブ変数待ち条件、セッション横断ジョブ待ち条件を持つ場合、
					//待ち合わせ解除を定期的にチェックするためにフラグをtrueにする
					jobSessionJobEntity.setWaitCheckFlg(waitCheckFlg);
					m_log.info("waitCheckFlg = " + waitCheckFlg);

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
					JobMstEntity jobunit = FullJob.getJobMstEntity(jobunitId, jobunitId);
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
							em.persist(jobParamInfoEntity);
							jobParamInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
					}
					// 監視管理の情報、ジョブ契機（ファイルチェック）情報を設定
					for (Map.Entry<String, String> entry : ParameterUtil.createParamInfo(info, triggerInfo).entrySet()) {
						JobParamInfoEntityPK jobParamInfoEntityPK
						= new JobParamInfoEntityPK(jobInfoEntity.getId().getSessionId(),
								jobInfoEntity.getId().getJobunitId(),
								jobInfoEntity.getId().getJobId(),
								entry.getKey());
						JobParamInfoEntity jobParamInfoEntity
						= em.find(JobParamInfoEntity.class, jobParamInfoEntityPK, ObjectPrivilegeMode.READ);
						if (jobParamInfoEntity == null) {
							jobParamInfoEntity = new JobParamInfoEntity(jobInfoEntity, entry.getKey());
							em.persist(jobParamInfoEntity);
							jobParamInfoEntity.relateToJobInfoEntity(jobInfoEntity);
						}
						jobParamInfoEntity.setValue(entry.getValue());
						jobParamInfoEntity.setDescription("");
						jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_SYSTEM_JOB);
					}
					// ジョブ契機（ジョブスケジュール、ファイルチェック、ジョブ連携受信実行契機）ランタイムジョブ変数デフォルト情報を設定
					if (jobSessionEntity.getTriggerType() == JobTriggerTypeConstant.TYPE_SCHEDULE
							|| jobSessionEntity.getTriggerType() == JobTriggerTypeConstant.TYPE_FILECHECK
							|| jobSessionEntity.getTriggerType() == JobTriggerTypeConstant.TYPE_JOBLINKRCV) {
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
									em.persist(jobParamInfoEntity);
									jobParamInfoEntity.relateToJobInfoEntity(jobInfoEntity);
								}
								jobParamInfoEntity.setValue(jobRuntimeParamEntity.getDefaultValue());
								jobParamInfoEntity.setDescription(jobRuntimeParamEntity.getDescription());
								jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_RUNTIME);
							}
						}
					}
					// ランタイムジョブ変数を設定
					if (triggerInfo.getJobRuntimeParamList() != null) {
						for (JobRuntimeParamRun JobRuntimeParamRun : triggerInfo.getJobRuntimeParamList()) {
							JobParamInfoEntityPK jobParamInfoEntityPK
								= new JobParamInfoEntityPK(jobInfoEntity.getId().getSessionId(),
										jobInfoEntity.getId().getJobunitId(),
										jobInfoEntity.getId().getJobId(),
										JobRuntimeParamRun.getParamId());
							JobParamInfoEntity jobParamInfoEntity
							= em.find(JobParamInfoEntity.class, jobParamInfoEntityPK, ObjectPrivilegeMode.READ);
							if (jobParamInfoEntity == null) {
								jobParamInfoEntity = new JobParamInfoEntity(jobInfoEntity, JobRuntimeParamRun.getParamId());
								em.persist(jobParamInfoEntity);
								jobParamInfoEntity.relateToJobInfoEntity(jobInfoEntity);
							}
							jobParamInfoEntity.setValue(JobRuntimeParamRun.getValue());
							jobParamInfoEntity.setDescription("");
							if (triggerInfo.getJobkickId() != null) {
								JobRuntimeParamEntityPK jobRuntimeParamEntityPK = new JobRuntimeParamEntityPK(triggerInfo.getJobkickId(), JobRuntimeParamRun.getParamId());
								JobRuntimeParamEntity jobRuntimeParamEntity = em.find(JobRuntimeParamEntity.class, jobRuntimeParamEntityPK, ObjectPrivilegeMode.READ);
								if (jobRuntimeParamEntity != null) {
									jobParamInfoEntity.setDescription(jobRuntimeParamEntity.getDescription());
								}
							}		
							jobParamInfoEntity.setParamType(JobParamTypeConstant.TYPE_RUNTIME);
						}
					}
				}

				//ファシリティパスを設定(JobSessionJobEntity)
				if (jobInfoEntity.getJobType() == JobConstant.TYPE_APPROVALJOB){
					jobSessionJobEntity.setScopeText(jobSessionJobEntity.getOwnerRoleId());
				} else if(jobInfoEntity.getFacilityId() != null
						&& !jobInfoEntity.getFacilityId().isEmpty()){
					//ファシリティID取得
					String facilityId = jobInfoEntity.getFacilityId();
					if(jobInfoEntity.getJobType() == JobConstant.TYPE_JOB || 
							jobInfoEntity.getJobType() == JobConstant.TYPE_MONITORJOB ||
							jobInfoEntity.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB ||
							jobInfoEntity.getJobType() == JobConstant.TYPE_FILECHECKJOB ||
							jobInfoEntity.getJobType() == JobConstant.TYPE_RESOURCEJOB ||
							jobInfoEntity.getJobType() == JobConstant.TYPE_RPAJOB) {
						facilityId = ParameterUtil.replaceFacilityId(sessionId, facilityId);
					}
					String scopePath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
					jobSessionJobEntity.setScopeText(scopePath);
				} else {
					jobSessionJobEntity.setScopeText(null);
				}

				// 承認ジョブにおけるJobSessionNodeEntityを生成、及びジョブ変数の反映
				if(job.getJobType() == JobConstant.TYPE_APPROVALJOB){
					// ファシリティID/ファシリティ名取得(承認ジョブの場合はオーナーロールID相当)
					RoleInfo roleInfo = new AccessControllerBean().getRoleInfo(jobSessionJobEntity.getOwnerRoleId());
					JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, jobSessionJobEntity.getOwnerRoleId());
					// 重複チェック
					jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
					if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_PREMAKESESSION) {
						// 事前生成時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
					} else {
						// ジョブ実行時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
					}
					jobSessionNodeEntity.setStartDate(null);
					jobSessionNodeEntity.setEndDate(null);
					jobSessionNodeEntity.setEndValue(null);
					jobSessionNodeEntity.setMessage(null);
					jobSessionNodeEntity.setRetryCount(0);
					jobSessionNodeEntity.setResult(null);
					jobSessionNodeEntity.setNodeName(roleInfo.getRoleName());
					// 登録
					em.persist(jobSessionNodeEntity);
					jobSessionNodeEntity.relateToJobSessionJobEntity(jobSessionJobEntity);
					m_log.debug("getRoleName():" + roleInfo.getRoleName());
					// 実行ユーザ設定(手動実行の場合のみ設定)
					if(jobSessionEntity.getTriggerType() == JobTriggerTypeConstant.TYPE_MANUAL){
						int pre = jobSessionEntity.getTriggerInfo().indexOf("(");
						int post = jobSessionEntity.getTriggerInfo().length()-1;
						String userid = jobSessionEntity.getTriggerInfo().substring(pre+1, post);
						m_log.debug("userid:" + userid);
						jobSessionNodeEntity.setApprovalRequestUser(userid);
					}
					
					String reqSentence = jobInfoEntity.getApprovalReqSentence(); // 承認依頼文
					String mailTitle = jobInfoEntity.getApprovalReqMailTitle(); // 承認依頼メール件名
					String mailBody = jobInfoEntity.getApprovalReqMailBody(); // 承認依頼メール本文
					/** 
					 * jobIdMapにリネーム情報がある場合、、上位が参照ジョブネットであるため、
					 * 承認ジョブに変数が含めていたら（承認依頼文、承認依頼メール件名、承認依頼メール文）
					 * 変数のジョブIDのところをリネームする
					 */
					if(jobIdMap != null && !jobIdMap.isEmpty()){
						reqSentence = renameJobIdInVariable(reqSentence, jobIdMap);
						mailTitle = renameJobIdInVariable(mailTitle, jobIdMap);
						mailBody = renameJobIdInVariable(mailBody, jobIdMap);
					}
				}

				// ジョブ連携待機ジョブにおけるJobSessionNodeEntityを制定
				if(job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB) {
					//ファシリティID取得
					String facilityId = ParameterUtil.replaceFacilityId(sessionId, job.getFacilityId());
					try {
						// 存在確認
						com.clustercontrol.repository.util.QueryUtil.getFacilityPK_NONE(facilityId);
					// 送信元スコープのセッションノード1つ用意する
						String scopePath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
						// インスタンス生成
						JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, facilityId);
						// 重複チェック
						jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
						if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_PREMAKESESSION) {
							// 事前生成時
							jobSessionNodeEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
						} else {
							// ジョブ実行時
							jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
						}
						jobSessionNodeEntity.setStartDate(null);
						jobSessionNodeEntity.setEndDate(null);
						jobSessionNodeEntity.setEndValue(null);
						jobSessionNodeEntity.setMessage(null);
						jobSessionNodeEntity.setRetryCount(0);
						jobSessionNodeEntity.setResult(null);
						jobSessionNodeEntity.setNodeName(HinemosMessage.replace(scopePath, Locale.getDefault()));
						// 登録
						em.persist(jobSessionNodeEntity);
						jobSessionNodeEntity.relateToJobSessionJobEntity(jobSessionJobEntity);
					} catch (FacilityNotFound e) {
						m_log.warn("createJobsessionJob() : facility is not found."
								+ " pk=" + jobSessionJobEntity.getId()
								+ ", facilityId=" + facilityId);
					}
				}

				// リソース制御ジョブにおけるJobSessionNodeEntityを生成
				if (job.getJobType() == JobConstant.TYPE_RESOURCEJOB) {
					// インスタンス生成
					String facilityId = job.getFacilityId();
					JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, facilityId);
					// 重複チェック
					jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
					if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_PREMAKESESSION) {
						// 事前生成時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
					} else {
						// ジョブ実行時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
					}
					jobSessionNodeEntity.setStartDate(null);
					jobSessionNodeEntity.setEndDate(null);
					jobSessionNodeEntity.setEndValue(null);
					jobSessionNodeEntity.setMessage(null);
					jobSessionNodeEntity.setRetryCount(0);
					jobSessionNodeEntity.setResult(null);
					String scopePath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
					jobSessionNodeEntity.setNodeName(HinemosMessage.replace(scopePath, Locale.getDefault()));
					// 登録
					em.persist(jobSessionNodeEntity);
					jobSessionNodeEntity.relateToJobSessionJobEntity(jobSessionJobEntity);
				}

				// RPAシナリオジョブ（間接実行）におけるJobSessionNodeEntity作成
				if(job.getJobType() == JobConstant.TYPE_RPAJOB 
						&& job.getRpaJobType() == RpaJobTypeConstant.INDIRECT){
					// ファシリティID(RPAシナリオジョブの場合はRPA管理ツールとして登録するスコープのファシリティID)
					RpaManagementToolMst master = com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolMstPK(
							job.getRpaManagementToolAccount().getRpaManagementToolId());
					JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity,
							RpaUtil.generateRpaManagementScopeId(job.getRpaManagementToolAccount(), master));
					// 重複チェック
					jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
					if (triggerInfo.getTrigger_type() == JobTriggerTypeConstant.TYPE_PREMAKESESSION) {
						// 事前生成時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
					} else {
						// ジョブ実行時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
					}
					jobSessionNodeEntity.setStartDate(null);
					jobSessionNodeEntity.setEndDate(null);
					jobSessionNodeEntity.setEndValue(null);
					jobSessionNodeEntity.setMessage(null);
					jobSessionNodeEntity.setRetryCount(0);
					jobSessionNodeEntity.setResult(null);
					jobSessionNodeEntity.setNodeName(job.getRpaManagementToolAccount().getRpaScopeName());
					// 登録
					em.persist(jobSessionNodeEntity);
					jobSessionNodeEntity.relateToJobSessionJobEntity(jobSessionJobEntity);
				}

				//後続ジョブ優先度を設定(JobNextJobOrderInfoEntity)
				Boolean exclusiveBranchFlg = job.getExclusiveBranchFlg();
				if (exclusiveBranchFlg != null && exclusiveBranchFlg == true) {
					List<JobNextJobOrderMstEntity> jobNextJobOrderMstEntityList = QueryUtil.getJobNextJobOrderMstEntityFindByJobunitIdJobId(jobunitId, jobId);

					for(JobNextJobOrderMstEntity jobNextJobOrderMstEntity: jobNextJobOrderMstEntityList) {
						JobNextJobOrderInfoEntityPK jobNextJobOrderInfoEntityPK = new JobNextJobOrderInfoEntityPK(
								jobInfoEntity.getId().getSessionId(),
								jobNextJobOrderMstEntity.getId().getJobunitId(),
								jobNextJobOrderMstEntity.getId().getJobId(),
								jobNextJobOrderMstEntity.getId().getNextJobId());

						JobNextJobOrderInfoEntity jobNextJobOrderInfoEntity
							= em.find(JobNextJobOrderInfoEntity.class, jobNextJobOrderInfoEntityPK, ObjectPrivilegeMode.READ);

						if (jobNextJobOrderInfoEntity == null) {
							jobNextJobOrderInfoEntity = new JobNextJobOrderInfoEntity(jobNextJobOrderInfoEntityPK);
							// 登録
							em.persist(jobNextJobOrderInfoEntity);
						}
						jobNextJobOrderInfoEntity.setOrder(jobNextJobOrderMstEntity.getOrder());
					}
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
				JobInfo tmpJobInfo = null;
				if(referJob != null){
					//参照ジョブの場合
				    tmpJobInfo = FullJob.getJobFullFromCache(referJob.getReferJobUnitId(), referJob.getReferJobId());
				} else if (referJobnetChildJobId != null) {
                    //参照ジョブネット配下のジョブの場合
                    tmpJobInfo = FullJob.getJobFullFromCache(jobunitId, referJobnetChildJobId);					
				} else {
				    tmpJobInfo = FullJob.getJobFullFromCache(jobunitId, jobId);
				}
				if (tmpJobInfo != null) {
					List<NotifyRelationInfo> ct = tmpJobInfo.getNotifyRelationInfos();
					// JobNoticeInfo用の通知グループIDで、通知関連テーブルのコピーを作成する
					List<NotifyRelationInfo> infoNotifyRelationList = new ArrayList<>();
					if (ct != null) {
						for (NotifyRelationInfo relation : ct) {
							NotifyRelationInfo nri = new NotifyRelationInfo(infoNotifyGroupId, relation.getNotifyId());
							em.persist(nri);
							nri.setNotifyType(relation.getNotifyType());
							nri.setFunctionPrefix(FunctionPrefixEnum.JOB_SESSION.name());
							infoNotifyRelationList.add(nri);
						}
					}
				}

				//ファイル転送を行うジョブネットの実行用情報を作成
				// 通知の設定が済んでいる必要があるため、この位置で設定を行う
				if(jobInfoEntity.getJobType() == JobConstant.TYPE_FILEJOB) {
					if (CreateHulftJob.isHulftMode()) {
						new CreateHulftJob().createHulftUtliupdtSJob(jobInfoEntity);
						// ノード／スコープを含めた情報を登録
						if (!jobSessionEntity.getExpNodeRuntimeFlg()) {
							new CreateHulftJob().createHulftFileJobNet(sessionId, jobunitId, jobId);
						}
					} else {
						// 参照ジョブの場合も通知マスタ取得のために、参照先のファイル転送ジョブのIDを引数で渡す
						new CreateFileJob().createGetFileListJob(jobInfoEntity, jobInfoEntity.getId().getJobId());
					}
				}

				// ノード／スコープを含めた情報を登録
				if (!jobSessionEntity.getExpNodeRuntimeFlg()) {
					createJobSessionNode(jobInfoEntity);
				}

				HashMap<String, String> referJobIdMap = null;
				//参照ジョブネットの場合
				if(referJob != null && referJob.getJobType() == JobConstant.TYPE_REFERJOBNET){
					// ジョブネット配下のジョブのリネーム情報は以下で一括取得する(リネーム文字列のプレフィックスは参照JOBネットのジョブID)
					// 参照先配下には参照ジョブネットは登録出来ないため、リネーム情報の作成は参照ジョブネット毎に1度のみとなる
					referJobIdMap = getRenameJobIdMap(sessionId, referJob.getId().getJobId(), job, null);
				} else {
					referJobIdMap = jobIdMap;
				}

				// 子供を再帰的に作成
				List<String> childJobMstIdList = jobMstChildrenCache.get(sessionId).get(job.getId().getJobunitId()).get(job.getId().getJobId());
				if (childJobMstIdList != null) {
					for(String childId : childJobMstIdList) {
						// リネームリストを渡す
						createJobSessionJob(job.getId().getJobunitId(), childId, sessionId, info, false, triggerInfo, referJob, referJobIdMap, existsJobSession);
					}
				}
			}

			m_log.debug("createJobSessionJob(), end, jobSessionJobEntity.getId()=" + jobSessionJobEntity.getId());
			return jobSessionJobEntity.getId();
		}
	}

	/**
	 * 参照ジョブネットの参照先ジョブネット配下のジョブIDをリネームするために
	 * リネーム前後のジョブIDのマッピング情報を作成する。
	 *
	 * @param sessionId		セッションID
	 * @param prefix 		JobIdリネーム用プレフィックス文字列(参照ジョブネットのジョブID)
	 * @param parentJob 	チェック対象の親ジョブ
	 * @param jobIdMap 		リネーム前後のジョブIDのマッピング情報
	 * @return
	 * @throws InvalidRole 
	 * @throws JobMasterNotFound 
	 */
	private static HashMap<String, String> getRenameJobIdMap(String sessionId, String prefix, JobMstEntity parentJob, HashMap<String, String> jobIdMap) throws JobMasterNotFound, InvalidRole {
		m_log.debug("getRenameJobIdMap() " + ": sessionId=" + sessionId +  ", prefix=" + prefix + ", parentJob=" + parentJob + ", jobIdMap=" + jobIdMap);

		if(jobIdMap == null){
			jobIdMap = new HashMap<String, String>();
		}
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			List<String> childJobMstIdList = jobMstChildrenCache.get(sessionId).get(parentJob.getId().getJobunitId()).get(parentJob.getId().getJobId());
			if (childJobMstIdList != null) {
				for(String childId : childJobMstIdList) {
					JobMstEntity childJob = FullJob.getJobMstEntity(parentJob.getId().getJobunitId(), childId);
					
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
					
					jobIdMap = getRenameJobIdMap(sessionId, prefix, childJob, jobIdMap);
				}
			}
			return jobIdMap;
		}
	}

	/**
	 * ジョブセッションノードを生成
	 * 
	 * @throws HinemosUnknown
	 * @throws InvalidRole 
	 */
	public static void createJobSessionNode(JobInfoEntity jobInfoEntity)
		throws HinemosUnknown, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			JobSessionJobEntity jobSessionJobEntity = jobInfoEntity.getJobSessionJobEntity();
			String sessionId = jobInfoEntity.getId().getSessionId();

			if(jobInfoEntity.getJobType() == JobConstant.TYPE_JOB || 
					jobInfoEntity.getJobType() == JobConstant.TYPE_MONITORJOB ||
					jobInfoEntity.getJobType() == JobConstant.TYPE_FILECHECKJOB ||
					jobInfoEntity.getJobType() == JobConstant.TYPE_RPAJOB) {
				//ファシリティID取得
				String facilityId = ParameterUtil.replaceFacilityId(sessionId, jobInfoEntity.getFacilityId());
				ArrayList<String> nodeIdList = new ArrayList<String>();
				if(facilityId != null){
					//ノードのファシリティIDリスト取得
					nodeIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, jobSessionJobEntity.getOwnerRoleId());
				}

				boolean isRpaManagementToolServiceMonitorJob = false;
				if (jobInfoEntity.getJobType() == JobConstant.TYPE_MONITORJOB) {
					try {
						MonitorInfo monitorInfo 
						= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(jobInfoEntity.getMonitorId());
						if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE)) {
							// RPA管理ツールアカウントサービス監視の場合、アカウントを表すスコープを対象とするため、
							// ここではJobSessionNodeEntityを作成しない(後で作成)
							isRpaManagementToolServiceMonitorJob = true;
							nodeIdList.clear();
						}
					} catch (MonitorNotFound e) {
						m_log.warn("createJobSessionJob() : monitorInfo of MonitorJob not found. monitorId=" + jobInfoEntity.getMonitorId());
						throw new HinemosUnknown(e);
					}
				}

				if (isRpaManagementToolServiceMonitorJob) {
					// RPA管理ツールアカウントサービス監視の場合、ここでスコープ単位のJobSessionNodeEntityを作成
					FacilityInfo facilityInfo = new RepositoryControllerBean().getFacilityEntityByPK(facilityId);
					// インスタンス生成
					JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, facilityId);
					// 重複チェック
					jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
					if (jobSessionJobEntity.getStatus() == StatusConstant.TYPE_SCHEDULED) {
						// 事前生成時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
					} else {
						// ジョブ実行時
						jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
					}
					jobSessionNodeEntity.setStartDate(null);
					jobSessionNodeEntity.setEndDate(null);
					jobSessionNodeEntity.setEndValue(null);
					jobSessionNodeEntity.setMessage(null);
					jobSessionNodeEntity.setRetryCount(0);
					jobSessionNodeEntity.setResult(null);
					jobSessionNodeEntity.setNodeName(facilityInfo.getFacilityName());
					// 登録
					em.persist(jobSessionNodeEntity);
					jobSessionNodeEntity.relateToJobSessionJobEntity(jobSessionJobEntity);
				}
				
				// RPAシナリオジョブ(間接実行)の場合、ノード詳細はスコープのみとし、スコープ配下のノードに対しては作成しない
				if(jobInfoEntity.getJobType() == JobConstant.TYPE_RPAJOB && 
						jobInfoEntity.getRpaJobType() == RpaJobTypeConstant.INDIRECT) {
					nodeIdList.clear();
				}
				
				if(nodeIdList != null){
					for(String nodeId : nodeIdList){
						//ノード名を取得
						NodeInfo nodeInfo = new RepositoryControllerBean().getNode(nodeId);
						// RPAシナリオジョブの場合はWINDOWSのみ実行対象
						if (jobInfoEntity.getJobType() == JobConstant.TYPE_RPAJOB 
								&& !nodeInfo.getPlatformFamily().equals(PlatformConstant.WINDOWS)) {
							m_log.warn("createJobSessionJob() : rpa scenario job invalid os platform, facilityId=" + nodeInfo.getFacilityId());
							continue;
						}
						// インスタンス生成
						JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, nodeId);
						// 重複チェック
						jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
						if (jobSessionJobEntity.getStatus() == StatusConstant.TYPE_SCHEDULED) {
							// 事前生成時
							jobSessionNodeEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
						} else {
							// ジョブ実行時
							jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
						}
						jobSessionNodeEntity.setStartDate(null);
						jobSessionNodeEntity.setEndDate(null);
						jobSessionNodeEntity.setEndValue(null);
						jobSessionNodeEntity.setMessage(null);
						jobSessionNodeEntity.setRetryCount(0);
						jobSessionNodeEntity.setResult(null);
						jobSessionNodeEntity.setNodeName(nodeInfo.getFacilityName());
						// 登録
						em.persist(jobSessionNodeEntity);
						jobSessionNodeEntity.relateToJobSessionJobEntity(jobSessionJobEntity);
					}
				}
			} else if (jobInfoEntity.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB) {
				// ジョブ連携送信設定情報取得
				JobLinkSendSettingEntity settingEntity
					= new JobControllerBean().getJobLinkSendSetting(jobInfoEntity.getJoblinkSendSettingId());

				ArrayList<String> nodeIdList = new ArrayList<String>();
				if(settingEntity.getFacilityId() != null){
					//ノードのファシリティIDリスト取得
					nodeIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(settingEntity.getFacilityId(), settingEntity.getOwnerRoleId());
				}
				if(nodeIdList != null){
					for(String nodeId : nodeIdList){
						//ノード名を取得
						NodeInfo nodeInfo = new RepositoryControllerBean().getNode(nodeId);
						// インスタンス生成
						JobSessionNodeEntity jobSessionNodeEntity = new JobSessionNodeEntity(jobSessionJobEntity, nodeId);
						// 重複チェック
						jtm.checkEntityExists(JobSessionNodeEntity.class, jobSessionNodeEntity.getId());
						if (jobSessionJobEntity.getStatus() == StatusConstant.TYPE_SCHEDULED) {
							// 事前生成時
							jobSessionNodeEntity.setStatus(StatusConstant.TYPE_SCHEDULED);
						} else {
							// ジョブ実行時
							jobSessionNodeEntity.setStatus(StatusConstant.TYPE_WAIT);
						}
						jobSessionNodeEntity.setStartDate(null);
						jobSessionNodeEntity.setEndDate(null);
						jobSessionNodeEntity.setEndValue(null);
						jobSessionNodeEntity.setMessage(null);
						jobSessionNodeEntity.setRetryCount(0);
						jobSessionNodeEntity.setResult(null);
						jobSessionNodeEntity.setNodeName(nodeInfo.getFacilityName());
						// 登録
						em.persist(jobSessionNodeEntity);
						jobSessionNodeEntity.relateToJobSessionJobEntity(jobSessionJobEntity);
					}
				}
			}
		} catch (JobMasterNotFound | JobInfoNotFound | FacilityNotFound | EntityExistsException | InvalidRole e) {
			m_log.warn("createJobSessionNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e);
		}
	}
	
	
	/**
	 * 参照ジョブネットの参照元ジョブネット配下のジョブのパラメータに 
	 * ジョブ変数（#[RETURN:jobid:facilityId]）を含め、
	 * 参照元ジョブネット配下のジョブと同じジョブIDが設定されている場合ジョブID（jobid）をリネームする。
	 * リネーム後のジョブIDはjobIdMapから取得する。
	 * 
	 * @param source
	 * @param jobIdMap
	 * @return
	 */
	private static String renameJobIdInVariable (String source, HashMap<String, String> jobIdMap) {
		Pattern pattern = Pattern.compile(ParameterUtil.REGEX_RETURN);
		String toReplace = source;
		Matcher matcher = pattern.matcher(toReplace);
		if (matcher.find()) { //REGEX_RETURNのパターンが見つかった
			String jobId = matcher.group(1);
			String rJobId = jobIdMap.get(jobId);
			if (rJobId != null){ //参照元ジョブネット配下のジョブが設定されていない
				source = source.replace(jobId, rJobId);
				m_log.debug("renameJobIdInVariable(): JobID changed in the variable: " + jobId + " to " + rJobId);
			}
		}
		pattern = Pattern.compile(ParameterUtil.REGEX_END_NUM);
		toReplace = source;
		matcher = pattern.matcher(toReplace);
		if (matcher.find()) { //REGEX_END_NUMのパターンが見つかった
			String jobId = matcher.group(1);
			String rJobId = jobIdMap.get(jobId);
			if (rJobId != null){ //参照元ジョブネット配下のジョブが設定されていない
				source = source.replace(jobId, rJobId);
				m_log.debug("renameJobIdInVariable(): JobID changed in the variable: " + jobId + " to " + rJobId);
			}
		}
		return source;
	}
}
