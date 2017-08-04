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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.MonitorJobInfo;
import com.clustercontrol.jobmanagement.model.JobCommandParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartParamMstEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ジョブ情報を検索するクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class FullJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( FullJob.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(FullJob.class.getName());
		
		try {
			init();
		} catch (Throwable t) {
			m_log.error("FullJob initialisation error. " + t.getMessage(), t);
		}
	}
	
	public static void init() {
		try {
			_lock.writeLock();
			
			Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
			if (jobMstCache == null) {	// not null if clustered
				initJobMstCache();
			}
		} finally {
			_lock.writeUnlock();
		}
		
		try {
			_lock.writeLock();
			
			Map<String, Map<String, JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache == null) {	// not null if clustered
				initJobInfoCache();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	public static void initJobMstCache() {
		long startTime = System.currentTimeMillis();
		HashMap<String, Map<String,JobMstEntity>> jobMstCache = new HashMap<String, Map<String,JobMstEntity>>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			EntityManager em = jtm.getEntityManager();
			em.clear();
			List<JobMstEntity> jobunits = ((HinemosEntityManager)em).createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
					.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
					.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
					.getResultList();
			
			for(JobMstEntity jobunit : jobunits) {
				String jobunitId = jobunit.getId().getJobunitId();
				List<JobMstEntity> jobs =
						((HinemosEntityManager)em).createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				Map<String, JobMstEntity> jobunitMap = new HashMap<String, JobMstEntity>();
				for(JobMstEntity job : jobs) {
					String jobId = job.getId().getJobId();
					jobunitMap.put(jobId, job);
				}
				jobMstCache.put(jobunitId, jobunitMap);
			}
			m_log.info("init jobMstCache " + (System.currentTimeMillis() - startTime) + "ms. size=" + jobMstCache.size());
			for(Map.Entry<String, Map<String, JobMstEntity>> entry : jobMstCache.entrySet()) {
				m_log.info("jobMstCache key(jobunitId)=" + entry.getKey() + " size=" + entry.getValue().size());
			}
			
			storeJobMstCache(jobMstCache);
		} finally {
			if(jtm != null) {
				jtm.close();
			}
		}
	}
	
	public static void initJobInfoCache() {
		long startTime = System.currentTimeMillis();
		HashMap<String, Map<String,JobInfo>> jobInfoCache = new HashMap<String, Map<String,JobInfo>>();
		
		Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
		
		for(Map.Entry<String, Map<String, JobMstEntity>> jobunitEntry : jobMstCache.entrySet()) {
			String jobunitId = jobunitEntry.getKey();
			Map<String, JobInfo> jobunitMap = new HashMap<String, JobInfo>();
			for(Map.Entry<String, JobMstEntity> jobEntry : jobunitEntry.getValue().entrySet()) {
				String jobId = jobEntry.getKey();
				try {
					jobunitMap.put(jobId, createJobInfo(jobEntry.getValue()));
				} catch (InvalidRole | JobMasterNotFound | HinemosUnknown e) {
					m_log.warn("failed initCache jobunitId=" + jobunitId + " jobId=" + jobId + ". " 
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
				}
			}
			jobInfoCache.put(jobunitId, jobunitMap);
		}
		
		m_log.info("init jobInfoCache " + (System.currentTimeMillis() - startTime) + "ms. size=" + jobInfoCache.size());
		for(String jobunitId : jobMstCache.keySet()) {
			m_log.info("jobInfoCache key(jobunitId)=" + jobunitId + " size=" + jobInfoCache.get(jobunitId).size());
		}
		
		storeJobInfoCache(jobInfoCache);
	}
	
	/**
	 * 高速化のためのキャッシュ
	 * <jobunitId, <jobId, jobInfo>>
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobInfo>> getJobInfoCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_INFO);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobInfo>>)cache;
	}
	
	private static void storeJobInfoCache(HashMap<String, Map<String, JobInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_INFO, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobMstEntity>> getJobMstCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_MST);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_MST + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobMstEntity>>)cache;
	}
	
	private static void storeJobMstCache(HashMap<String, Map<String, JobMstEntity>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_MST + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_MST, newCache);
	}

	// deleteJobunitから呼ばれる
	public static void removeCache(String jobunitId) {
		m_log.debug("removeCache " + jobunitId);
		
		try {
			_lock.writeLock();
			
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache.remove(jobunitId) != null) {
				storeJobInfoCache(jobInfoCache);
			}
			
			HashMap<String, Map<String,JobMstEntity>> jobMstCache = getJobMstCache();
			if (jobMstCache.remove(jobunitId) != null) {
				storeJobMstCache(jobMstCache);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	// registerJobunitから呼ばれる。
	public static void updateCache(String jobunitId, List<JobInfo> infos) {
		m_log.debug("updateCache " + jobunitId);
		
		try {
			_lock.writeLock();

			HashMap<String, Map<String,JobMstEntity>> jobMstCache = getJobMstCache();
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			
			Map<String,JobMstEntity> jobunitMstMap = new HashMap<String,JobMstEntity>();
			Map<String,JobInfo> jobunitInfoMap = new HashMap<String,JobInfo>();
			
			new JpaTransactionManager().getEntityManager().clear();
			for(JobInfo info : infos) {
				String jobId = info.getId();
				try {
					JobMstEntity job = QueryUtil.getJobMstPK(jobunitId, jobId);
					jobunitMstMap.put(jobId, job);
					jobunitInfoMap.put(jobId, createJobInfo(job));
				} catch (InvalidRole | JobMasterNotFound | HinemosUnknown e) {
					m_log.warn("failed initCache jobunitId=" + jobunitId + " jobId=" + jobId + ". " 
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
				}
			}
			jobMstCache.put(jobunitId, jobunitMstMap);
			jobInfoCache.put(jobunitId, jobunitInfoMap);
			
			storeJobMstCache(jobMstCache);
			storeJobInfoCache(jobInfoCache);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * ジョブ情報{@link com.clustercontrol.jobmanagement.bean.JobInfo}を作成します。<BR>
	 * ジョブマスタを基に、ジョブ情報を作成します。
	 *
	 * @param job ジョブマスタ
	 * @param treeOnly treeOnly true=ジョブ情報を含まない, false=ジョブ情報含む
	 * @return ジョブ情報
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 */
	public static JobInfo getJobFull(JobInfo jobInfo) throws HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidRole {
		m_log.debug("createJobData() : " + jobInfo.getJobunitId() + ", " + jobInfo.getId() + "," + jobInfo.isPropertyFull());
		if (jobInfo.isPropertyFull()) {
			return jobInfo;
		}

		String jobunitId = jobInfo.getJobunitId();
		String jobId = jobInfo.getId();
		
		
		try {
			_lock.readLock();
			
			Map<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			Map<String, JobInfo> jobInfoUnitCache = jobInfoCache.get(jobunitId);
			if (jobInfoUnitCache != null) {
				JobInfo ret = jobInfoUnitCache.get(jobId);
				if (ret != null) {
					m_log.debug("cache hit " + jobunitId + "," + jobId + ", hit=" + jobInfoUnitCache.size());
					return ret;
				}
			} else {
				m_log.debug("cache didn't hit " + jobunitId + "," + jobId);
			}
		} finally {
			_lock.readUnlock();
		}
		
		m_log.debug("createJobData() : " + jobunitId + ", " + jobId);
		JobMstEntity jobMstEntity = null;
		try {
			_lock.readLock();
			jobMstEntity = getJobMstEntityFromLocal(jobunitId, jobId);
			if (jobMstEntity == null) {
				jobMstEntity = QueryUtil.getJobMstPK(jobunitId, jobId);
			}
		} finally {
			_lock.readUnlock();
		}
		
		jobInfo = createJobInfo(jobMstEntity);
		
		return jobInfo;
	}

	/**
	 * ローカルからジョブの情報を読み込む。
	 * @param jobunitId
	 * @param jobId
	 * @return
	 */
	private static JobMstEntity getJobMstEntityFromLocal(String jobunitId,
			String jobId) {
		Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
		Map<String, JobMstEntity> jobMstUnitCache = jobMstCache.get(jobunitId);
		if (jobMstUnitCache == null) {
			return null;
		}
		return jobMstUnitCache.get(jobId);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobパラメータ関連の情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 */
	private static void setJobParam(JobInfo jobInfo, JobMstEntity jobMstEntity) {
		ArrayList<JobParameterInfo> paramList = new ArrayList<JobParameterInfo>();
		Collection<JobParamMstEntity> params = jobMstEntity.getJobParamMstEntities();
		if(params != null){
			for (JobParamMstEntity param : params) {
				JobParameterInfo paramInfo = new JobParameterInfo();
				paramInfo.setParamId(param.getId().getParamId());
				paramInfo.setType(param.getParamType());
				paramInfo.setDescription(param.getDescription());
				paramInfo.setValue(param.getValue());
				paramList.add(paramInfo);
			}
			/*
			 * ソート処理
			 */
			Collections.sort(paramList);
		}
		jobInfo.setParam(paramList);
	}

	/**
	 * jobMstEntityの情報に基づき、Job終了状態をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 */
	private static void setJobEndStatus(JobInfo jobInfo, JobMstEntity jobMstEntity) {
		//終了状態を取得
		ArrayList<JobEndStatusInfo> endList = new ArrayList<JobEndStatusInfo>();
		// 正常
		JobEndStatusInfo endInfoNormal = new JobEndStatusInfo();
		endInfoNormal.setType(EndStatusConstant.TYPE_NORMAL);
		endInfoNormal.setValue(jobMstEntity.getNormalEndValue());
		endInfoNormal.setStartRangeValue(jobMstEntity.getNormalEndValueFrom());
		endInfoNormal.setEndRangeValue(jobMstEntity.getNormalEndValueTo());
		endList.add(endInfoNormal);
		// 警告
		JobEndStatusInfo endInfoWarn = new JobEndStatusInfo();
		endInfoWarn.setType(EndStatusConstant.TYPE_WARNING);
		endInfoWarn.setValue(jobMstEntity.getWarnEndValue());
		endInfoWarn.setStartRangeValue(jobMstEntity.getWarnEndValueFrom());
		endInfoWarn.setEndRangeValue(jobMstEntity.getWarnEndValueTo());
		endList.add(endInfoWarn);
		// 異常
		JobEndStatusInfo endInfoAbnormal = new JobEndStatusInfo();
		endInfoAbnormal.setType(EndStatusConstant.TYPE_ABNORMAL);
		endInfoAbnormal.setValue(jobMstEntity.getAbnormalEndValue());
		endInfoAbnormal.setStartRangeValue(jobMstEntity.getAbnormalEndValueFrom());
		endInfoAbnormal.setEndRangeValue(jobMstEntity.getAbnormalEndValueTo());
		endList.add(endInfoAbnormal);
		
		jobInfo.setEndStatus(endList);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobの通知情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private static void setJobNotifications(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws InvalidRole, HinemosUnknown {
		jobInfo.setBeginPriority(jobMstEntity.getBeginPriority());
		jobInfo.setNormalPriority(jobMstEntity.getNormalPriority());
		jobInfo.setWarnPriority(jobMstEntity.getWarnPriority());
		jobInfo.setAbnormalPriority(jobMstEntity.getAbnormalPriority());

		//通知情報の取得
		ArrayList<NotifyRelationInfo> nriList = new NotifyControllerBean().getNotifyRelation(jobMstEntity.getNotifyGroupId());
		if (nriList != null) {
			Collections.sort(nriList);
			jobInfo.setNotifyRelationInfos(nriList);
		}
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのファイル情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setJobFile(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobFileInfo fileInfo = new JobFileInfo();
		fileInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		fileInfo.setSrcFacilityID(jobMstEntity.getSrcFacilityId());
		fileInfo.setDestFacilityID(jobMstEntity.getDestFacilityId());
		fileInfo.setSrcFile(jobMstEntity.getSrcFile());
		fileInfo.setSrcWorkDir(jobMstEntity.getSrcWorkDir());
		fileInfo.setDestDirectory(jobMstEntity.getDestDirectory());
		fileInfo.setDestWorkDir(jobMstEntity.getDestWorkDir());
		fileInfo.setCompressionFlg(jobMstEntity.getCompressionFlg());
		fileInfo.setCheckFlg(jobMstEntity.getCheckFlg());
		fileInfo.setSpecifyUser(jobMstEntity.getSpecifyUser());
		fileInfo.setUser(jobMstEntity.getEffectiveUser());
		fileInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		fileInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		fileInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		fileInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		fileInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		//ファシリティパスを取得
		fileInfo.setSrcScope(new RepositoryControllerBean().getFacilityPath(jobMstEntity.getSrcFacilityId(), null));
		fileInfo.setDestScope(new RepositoryControllerBean().getFacilityPath(jobMstEntity.getDestFacilityId(), null));
		jobInfo.setFile(fileInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのコマンド情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setJobCommand(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobCommandInfo commandInfo = new JobCommandInfo();
		commandInfo.setFacilityID(jobMstEntity.getFacilityId());
		commandInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		commandInfo.setStartCommand(jobMstEntity.getStartCommand());
		commandInfo.setStopType(jobMstEntity.getStopType());
		commandInfo.setStopCommand(jobMstEntity.getStopCommand());
		commandInfo.setSpecifyUser(jobMstEntity.getSpecifyUser());
		commandInfo.setUser(jobMstEntity.getEffectiveUser());
		commandInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		commandInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		commandInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		commandInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		commandInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		// ジョブ終了時のジョブ変数
		ArrayList<JobCommandParam> jobCommandParamList = new ArrayList<>();
		if (jobMstEntity.getJobCommandParamEntities() != null && jobMstEntity.getJobCommandParamEntities().size() > 0) {
			for (JobCommandParamMstEntity jobCommandParamEntity : jobMstEntity.getJobCommandParamEntities()) {
				if (jobMstEntity.getJobCommandParamEntities() != null) {
					JobCommandParam jobCommandParam = new JobCommandParam();
					jobCommandParam.setJobStandardOutputFlg(jobCommandParamEntity.getJobStandardOutputFlg());
					jobCommandParam.setParamId(jobCommandParamEntity.getId().getParamId());
					jobCommandParam.setValue(jobCommandParamEntity.getValue());
					jobCommandParamList.add(jobCommandParam);
				}
			}
		}
		commandInfo.setJobCommandParamList(jobCommandParamList);
		commandInfo.setManagerDistribution(jobMstEntity.getManagerDistribution());
		commandInfo.setScriptName(jobMstEntity.getScriptName());
		commandInfo.setScriptEncoding(jobMstEntity.getScriptEncoding());
		commandInfo.setScriptContent(jobMstEntity.getScriptContent());
		
		List<JobEnvVariableInfo> envVariableList = new ArrayList<JobEnvVariableInfo>();
		List<JobEnvVariableMstEntity> envVariables = jobMstEntity.getJobEnvVariableMstEntities();
		if(envVariables != null){
			for (JobEnvVariableMstEntity envVariable : envVariables) {
				JobEnvVariableInfo envVariableInfo = new JobEnvVariableInfo();
				envVariableInfo.setEnvVariableId(envVariable.getId().getEnvVariableId());
				envVariableInfo.setDescription(envVariable.getDescription());
				envVariableInfo.setValue(envVariable.getValue());
				envVariableList.add(envVariableInfo);
			}
			//ソート処理
			Collections.sort(envVariableList);
		}
		commandInfo.setEnvVariableInfo(envVariableList);
		
		//ファシリティパスを取得
		commandInfo.setScope(new RepositoryControllerBean().getFacilityPath(jobMstEntity.getFacilityId(), null));
		jobInfo.setCommand(commandInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobの監視ジョブ情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setMonitorJob(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		MonitorJobInfo monitorJobInfo = new MonitorJobInfo();
		monitorJobInfo.setFacilityID(jobMstEntity.getFacilityId());
		//ファシリティパスを取得
		monitorJobInfo.setScope(new RepositoryControllerBean().getFacilityPath(jobMstEntity.getFacilityId(), null));
		monitorJobInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		monitorJobInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		monitorJobInfo.setMonitorId(jobMstEntity.getMonitorId());
		monitorJobInfo.setMonitorInfoEndValue(jobMstEntity.getMonitorInfoEndValue());
		monitorJobInfo.setMonitorWarnEndValue(jobMstEntity.getMonitorWarnEndValue());
		monitorJobInfo.setMonitorCriticalEndValue(jobMstEntity.getMonitorCriticalEndValue());
		monitorJobInfo.setMonitorUnknownEndValue(jobMstEntity.getMonitorUnknownEndValue());
		monitorJobInfo.setMonitorWaitTime(jobMstEntity.getMonitorWaitTime());
		monitorJobInfo.setMonitorWaitEndValue(jobMstEntity.getMonitorWaitEndValue());
		monitorJobInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		monitorJobInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		jobInfo.setMonitor(monitorJobInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobの待ち条件をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private static void setJobWaitRule(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws JobMasterNotFound, InvalidRole {
		//待ち条件を取得
		JobWaitRuleInfo waitRule = null;
		//待ち条件を取得
		waitRule = new JobWaitRuleInfo();
		//ジョブネット・ジョブ・ファイル転送ジョブの場合
		//待ち条件を設定
		if(jobMstEntity.getJobType() == JobConstant.TYPE_JOBNET ||
				jobMstEntity.getJobType() == JobConstant.TYPE_JOB||
				jobMstEntity.getJobType() == JobConstant.TYPE_FILEJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_APPROVALJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_REFERJOBNET ||
				jobMstEntity.getJobType() == JobConstant.TYPE_REFERJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_MONITORJOB){
			waitRule.setSuspend(jobMstEntity.getSuspend());
			waitRule.setCondition(jobMstEntity.getConditionType());
			waitRule.setEndCondition(jobMstEntity.getUnmatchEndFlg());
			waitRule.setEndStatus(jobMstEntity.getUnmatchEndStatus());
			waitRule.setEndValue(jobMstEntity.getUnmatchEndValue());
			waitRule.setSkip(jobMstEntity.getSkip());
			waitRule.setSkipEndStatus(jobMstEntity.getSkipEndStatus());
			waitRule.setSkipEndValue(jobMstEntity.getSkipEndValue());
			waitRule.setCalendar(jobMstEntity.getCalendar());
			waitRule.setCalendarId(jobMstEntity.getCalendarId());
			waitRule.setCalendarEndStatus(jobMstEntity.getCalendarEndStatus());
			waitRule.setCalendarEndValue(jobMstEntity.getCalendarEndValue());

			waitRule.setStart_delay(jobMstEntity.getStartDelay());
			waitRule.setStart_delay_session(jobMstEntity.getStartDelaySession());
			waitRule.setStart_delay_session_value(jobMstEntity.getStartDelaySessionValue());
			waitRule.setStart_delay_time(jobMstEntity.getStartDelayTime());
			if (jobMstEntity.getStartDelayTimeValue() != null) {
				waitRule.setStart_delay_time_value(jobMstEntity.getStartDelayTimeValue());
			}
			waitRule.setStart_delay_condition_type(jobMstEntity.getStartDelayConditionType());
			waitRule.setStart_delay_notify(jobMstEntity.getStartDelayNotify());
			waitRule.setStart_delay_notify_priority(jobMstEntity.getStartDelayNotifyPriority());
			waitRule.setStart_delay_operation(jobMstEntity.getStartDelayOperation());
			waitRule.setStart_delay_operation_type(jobMstEntity.getStartDelayOperationType());
			waitRule.setStart_delay_operation_end_status(jobMstEntity.getStartDelayOperationEndStatus());
			waitRule.setStart_delay_operation_end_value(jobMstEntity.getStartDelayOperationEndValue());

			waitRule.setEnd_delay(jobMstEntity.getEndDelay());
			waitRule.setEnd_delay_session(jobMstEntity.getEndDelaySession());
			waitRule.setEnd_delay_session_value(jobMstEntity.getEndDelaySessionValue());
			waitRule.setEnd_delay_job(jobMstEntity.getEndDelayJob());
			waitRule.setEnd_delay_job_value(jobMstEntity.getEndDelayJobValue());
			waitRule.setEnd_delay_time(jobMstEntity.getEndDelayTime());
			if (jobMstEntity.getEndDelayTimeValue() != null) {
				waitRule.setEnd_delay_time_value(jobMstEntity.getEndDelayTimeValue());
			}
			waitRule.setEnd_delay_condition_type(jobMstEntity.getEndDelayConditionType());
			waitRule.setEnd_delay_notify(jobMstEntity.getEndDelayNotify());
			waitRule.setEnd_delay_notify_priority(jobMstEntity.getEndDelayNotifyPriority());
			waitRule.setEnd_delay_operation(jobMstEntity.getEndDelayOperation());
			waitRule.setEnd_delay_operation_type(jobMstEntity.getEndDelayOperationType());
			waitRule.setEnd_delay_operation_end_status(jobMstEntity.getEndDelayOperationEndStatus());
			waitRule.setEnd_delay_operation_end_value(jobMstEntity.getEndDelayOperationEndValue());
			waitRule.setMultiplicityNotify(jobMstEntity.getMultiplicityNotify());
			waitRule.setMultiplicityNotifyPriority(jobMstEntity.getMultiplicityNotifyPriority());
			waitRule.setMultiplicityOperation(jobMstEntity.getMultiplicityOperation());
			waitRule.setMultiplicityEndValue(jobMstEntity.getMultiplicityEndValue());
		}

		//待ち条件（ジョブ）を取得
		Collection<JobStartJobMstEntity> startJobList = jobMstEntity.getJobStartJobMstEntities();
		ArrayList<JobObjectInfo> objectList = new ArrayList<JobObjectInfo>();
		if(startJobList != null && startJobList.size() > 0){
			for (JobStartJobMstEntity startJob : startJobList){
				if(startJob != null){
					JobObjectInfo objectInfo = new JobObjectInfo();
					objectInfo.setJobId(startJob.getId().getTargetJobId());
					//対象ジョブを取得
					JobMstEntity targetJob = QueryUtil.getJobMstPK(startJob.getId().getTargetJobunitId(), startJob.getId().getTargetJobId());
					objectInfo.setJobName(targetJob.getJobName());
					objectInfo.setType(startJob.getId().getTargetJobType());
					objectInfo.setValue(startJob.getId().getTargetJobEndValue());
					objectInfo.setDescription(startJob.getTargetJobDescription());
					m_log.debug("getTargetJobType = " + startJob.getId().getTargetJobType());
					m_log.debug("getTargetJobId = " + startJob.getId().getTargetJobId());
					m_log.debug("getTargetJobEndValue = " + startJob.getId().getTargetJobEndValue());
					m_log.debug("getTargetJobDescription = " + startJob.getTargetJobDescription());
					objectList.add(objectInfo);
				}
			}
		}

		//待ち条件（時刻）を取得
		if (jobMstEntity.getStartTime() != null) {
			JobObjectInfo objectInfo = new JobObjectInfo();
			objectInfo.setType(JudgmentObjectConstant.TYPE_TIME);
			objectInfo.setTime(jobMstEntity.getStartTime());
			objectInfo.setDescription(jobMstEntity.getStartTimeDescription());
			m_log.debug("getType = " + JudgmentObjectConstant.TYPE_TIME);
			m_log.debug("getTime = " + jobMstEntity.getStartTime());
			m_log.debug("getStartTimeDescription= " + jobMstEntity.getStartTimeDescription());
			objectList.add(objectInfo);
		}
		m_log.debug("job.getStartMinute() = " + jobMstEntity.getStartMinute());
		//待ち条件（セッション開始時の時間（分））を取得
		if (jobMstEntity.getStartMinute() != null) {
			JobObjectInfo objectInfo = new JobObjectInfo();
			objectInfo.setType(JudgmentObjectConstant.TYPE_START_MINUTE);
			objectInfo.setStartMinute(jobMstEntity.getStartMinute());
			objectInfo.setDescription(jobMstEntity.getStartMinuteDescription());
			m_log.debug("getType = " + JudgmentObjectConstant.TYPE_START_MINUTE);
			m_log.debug("getStartMinute = " + jobMstEntity.getStartMinute());
			m_log.debug("getStartMinuteDescription= " + jobMstEntity.getStartMinuteDescription());
			objectList.add(objectInfo);
		}

		// 待ち条件（ジョブ変数）を取得
		List<JobStartParamMstEntity> decisionList = jobMstEntity.getJobStartParamMstEntities();
		if (decisionList != null && decisionList.size() != 0) {
			for (JobStartParamMstEntity decision : decisionList) {
				if (decision != null) {
					JobObjectInfo objectInfo = new JobObjectInfo();
					objectInfo.setType(decision.getId().getTargetJobType());
					objectInfo.setDecisionValue01(decision.getId().getStartDecisionValue01());
					objectInfo.setDecisionCondition(decision.getId().getStartDecisionCondition());
					objectInfo.setDecisionValue02(decision.getId().getStartDecisionValue02());
					objectInfo.setDescription(decision.getDecisionDescription());
					m_log.debug("getTargetJobType = " + decision.getId().getTargetJobType());
					m_log.debug("getStartDecisionValue01 = " + decision.getId().getStartDecisionValue01());
					m_log.debug("getStartDecisionCondition = " + decision.getId().getStartDecisionCondition());
					m_log.debug("getStartDecisionValue02 = " + decision.getId().getStartDecisionValue02());
					m_log.debug("getDecisionDescription = " + decision.getDecisionDescription());
					objectList.add(objectInfo);
				}
			}
		}
		/*
		 * ソート処理
		 */
		Collections.sort(objectList);
		waitRule.setObject(objectList);
		jobInfo.setWaitRule(waitRule);
	}

	/**
	 * ジョブツリー配下のジョブを全てfullProperty=trueにする。
	 * @param job
	 * @throws JobMasterNotFound
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void setJobTreeFull (JobTreeItem job)
			throws JobMasterNotFound, NotifyNotFound, UserNotFound, InvalidRole, HinemosUnknown {
		JobInfo jobInfo = job.getData();
		if (!jobInfo.isPropertyFull()) {
			job.setData(getJobFull(jobInfo));
		}
		if (job.getChildren() == null) {
			return;
		}
		for (JobTreeItem childJob : job.getChildren()) {
			setJobTreeFull(childJob);
		}
	}
	
	private static JobInfo createJobInfo(JobMstEntity jobMstEntity) throws InvalidRole, JobMasterNotFound, HinemosUnknown {
		JobInfo jobInfo = new JobInfo(jobMstEntity.getId().getJobunitId(), jobMstEntity.getId().getJobId(), jobMstEntity.getJobName(), jobMstEntity.getJobType());
		
		jobInfo.setDescription(jobMstEntity.getDescription());
		jobInfo.setIconId(jobMstEntity.getIconId());
		jobInfo.setOwnerRoleId(jobMstEntity.getOwnerRoleId());
		jobInfo.setRegisteredModule(jobMstEntity.isRegisteredModule());

		if (jobMstEntity.getRegDate() != null) {
			jobInfo.setCreateTime(jobMstEntity.getRegDate());
		}
		if (jobMstEntity.getUpdateDate() != null) {
			jobInfo.setUpdateTime(jobMstEntity.getUpdateDate());
		}
		jobInfo.setCreateUser(jobMstEntity.getRegUser());
		jobInfo.setUpdateUser(jobMstEntity.getUpdateUser());

		jobInfo.setIconId(jobMstEntity.getIconId());

		setJobWaitRule(jobInfo, jobMstEntity);

		switch (jobMstEntity.getJobType()) {
		case JobConstant.TYPE_JOB:
			setJobCommand(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_FILEJOB:
			setJobFile(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_JOBUNIT:
			setJobParam(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_REFERJOB:
		case JobConstant.TYPE_REFERJOBNET:
			jobInfo.setReferJobUnitId(jobMstEntity.getReferJobUnitId());
			jobInfo.setReferJobId(jobMstEntity.getReferJobId());
			jobInfo.setReferJobSelectType(jobMstEntity.getReferJobSelectType());
			break;
		case JobConstant.TYPE_APPROVALJOB:
			jobInfo.setApprovalReqRoleId(jobMstEntity.getApprovalReqRoleId());
			jobInfo.setApprovalReqUserId(jobMstEntity.getApprovalReqUserId());
			jobInfo.setApprovalReqSentence(jobMstEntity.getApprovalReqSentence());
			jobInfo.setApprovalReqMailTitle(jobMstEntity.getApprovalReqMailTitle());
			jobInfo.setApprovalReqMailBody(jobMstEntity.getApprovalReqMailBody());
			jobInfo.setUseApprovalReqSentence(jobMstEntity.isUseApprovalReqSentence());
			break;
		case JobConstant.TYPE_MONITORJOB:
			setMonitorJob(jobInfo, jobMstEntity);
			break;
		default:
			break;
		}
		if (jobInfo.getType() != JobConstant.TYPE_REFERJOB && jobInfo.getType() != JobConstant.TYPE_REFERJOBNET) {
			setJobNotifications(jobInfo, jobMstEntity);
			setJobEndStatus(jobInfo, jobMstEntity);
		}

		jobInfo.setPropertyFull(true);
		return jobInfo;
	}
}
