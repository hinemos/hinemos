/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.clustercontrol.jobmanagement.bean.JobFileCheckInfo;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkRcvInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkSendInfo;
import com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectGroupInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputType;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.MonitorJobInfo;
import com.clustercontrol.jobmanagement.bean.ResourceJobInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobCheckEndValueInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobEndValueConditionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobOptionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobRunParamInfo;
import com.clustercontrol.jobmanagement.model.JobCommandParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableMstEntity;
import com.clustercontrol.jobmanagement.model.JobLinkInheritMstEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobExpMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntity;
import com.clustercontrol.jobmanagement.model.JobOutputMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobWaitGroupMstEntity;
import com.clustercontrol.jobmanagement.model.JobWaitMstEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityManager;

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
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			EntityManager em = jtm.getEntityManager();
			em.clear();
			
			List<JobParamMstEntity> paramList = ((HinemosEntityManager)em).createNamedQuery("JobParamMstEntity.findAll", 
					JobParamMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobWaitGroupMstEntity> waitGroupList = ((HinemosEntityManager)em).createNamedQuery("JobWaitGroupMstEntity.findAll", 
					JobWaitGroupMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobCommandParamMstEntity> commandParamList = ((HinemosEntityManager)em).createNamedQuery("JobCommandParamMstEntity.findAll", 
					JobCommandParamMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobEnvVariableMstEntity> envVariableList = ((HinemosEntityManager)em).createNamedQuery("JobEnvVariableMstEntity.findAll", 
					JobEnvVariableMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobNextJobOrderMstEntity> nextJobOrderList = ((HinemosEntityManager)em).createNamedQuery("JobNextJobOrderMstEntity.findAll", 
					JobNextJobOrderMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			
			// ジョブ変数を保持するマップ <jobunitId, <jobId, List<JobParamMstEntity>>>
			Map<String, Map<String, List<JobParamMstEntity>>> paramMap = new HashMap<String, Map<String, List<JobParamMstEntity>>>();
			// 待ち条件を保持するマップ <jobunitId, <jobId, List<JobStartJobMstEntity>>>
			Map<String, Map<String, List<JobWaitGroupMstEntity>>> waitGroupMap = new HashMap<String, Map<String, List<JobWaitGroupMstEntity>>>();
			// 待ち条件を保持するマップ(ターゲットジョブがキー) <targetJobunitId, <targetJobId, List<JobStartJobMstEntity>>>
			Map<String, Map<String, List<JobWaitMstEntity>>> waitTargetJobMap = new HashMap<String, Map<String, List<JobWaitMstEntity>>>();
			// ジョブ終了時の変数設定を保持するマップ <jobunitId, <jobId, List<JobCommandParamMstEntity>>>
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap = new HashMap<String, Map<String, List<JobCommandParamMstEntity>>>();
			// 環境変数を保持するマップ <jobunitId, <jobId, List<JobEnvVariableMstEntity>>>
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap = new HashMap<String, Map<String, List<JobEnvVariableMstEntity>>>();
			// 後続ジョブ実行設定を保持するマップ <jobunitId, <jobId, List<JobNextJobOrderMstEntity>>>
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap = new HashMap<String, Map<String, List<JobNextJobOrderMstEntity>>>();
			// 通知グループを保持するマップ <notifyGroupId, List<NotifyRelationInfo>>
			Map<String, List<NotifyRelationInfo>> notifyRelMap = new HashMap<String, List<NotifyRelationInfo>>();
			
			// ジョブ変数マップの作成
			createParamMap(paramList, paramMap);
			
			// 待ち条件マップの作成
			createWaitGroupMap(waitGroupList, waitGroupMap);
			
			// 待ち条件マップの作成(ターゲットジョブがキー)
			createWaitTargetJobMap(waitGroupList, waitTargetJobMap);
			
			// ジョブ終了時の変数設定マップの作成
			createCommandParamMap(commandParamList, commandParamMap);
			
			// 環境変数マップの作成
			createEnvVariableMap(envVariableList, envVariableMap);
			
			// 後続ジョブ実行設定マップの作成
			createNextJobOrderMap(nextJobOrderList, nextJobOrderMap);
			
			// 通知グループマップの作成
			createNotifyRelationMap(notifyRelMap, em);
			
			Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
			
			for(Map.Entry<String, Map<String, JobMstEntity>> jobunitEntry : jobMstCache.entrySet()) {
				String jobunitId = jobunitEntry.getKey();
				Map<String, JobInfo> jobunitMap = new HashMap<String, JobInfo>();
				for(Map.Entry<String, JobMstEntity> jobEntry : jobunitEntry.getValue().entrySet()) {
					String jobId = jobEntry.getKey();
					try {
						jobunitMap.put(jobId, createJobInfo(jobEntry.getValue(), paramMap, waitGroupMap, waitTargetJobMap, commandParamMap, envVariableMap, nextJobOrderMap, notifyRelMap));
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
		} finally {
			if(jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * 高速化のためのキャッシュ
	 * <jobunitId, <jobId, jobInfo>>
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobInfo>> getJobInfoCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_INFO);
		if (m_log.isTraceEnabled()) m_log.trace("get cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobInfo>>)cache;
	}
	
	private static void storeJobInfoCache(HashMap<String, Map<String, JobInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isTraceEnabled()) m_log.trace("store cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_INFO, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobMstEntity>> getJobMstCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_MST);
		if (m_log.isTraceEnabled()) m_log.trace("get cache " + AbstractCacheManager.KEY_JOB_MST + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobMstEntity>>)cache;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobMstEntity>> getJobMstCacheWithoutDebugLog() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_MST);
		return cache == null ? null : (HashMap<String, Map<String, JobMstEntity>>)cache;
	}
	
	private static void storeJobMstCache(HashMap<String, Map<String, JobMstEntity>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isTraceEnabled()) m_log.trace("store cache " + AbstractCacheManager.KEY_JOB_MST + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_MST, newCache);
	}

	// deleteJobunitから呼ばれる
	public static void removeCache(String jobunitId) {
		m_log.trace("removeCache " + jobunitId);
		
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
	public static void updateCache(String jobunitId) {
		m_log.trace("updateCache " + jobunitId);
		long start = HinemosTime.currentTimeMillis();
		
		try {
			_lock.writeLock();

			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				HinemosEntityManager em = jtm.getEntityManager();

				HashMap<String, Map<String,JobMstEntity>> jobMstCache = getJobMstCache();
				HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
				
				Map<String,JobMstEntity> jobunitMstMap = new HashMap<String,JobMstEntity>();
				Map<String,JobInfo> jobunitInfoMap = new HashMap<String,JobInfo>();
				em.clear();
				
				List<JobParamMstEntity> paramList = ((HinemosEntityManager)em).createNamedQuery("JobParamMstEntity.findByJobunitId", 
						JobParamMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobWaitGroupMstEntity> waitGroupList = ((HinemosEntityManager)em).createNamedQuery("JobWaitGroupMstEntity.findByJobunitId", 
						JobWaitGroupMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobCommandParamMstEntity> commandParamList = ((HinemosEntityManager)em).createNamedQuery("JobCommandParamMstEntity.findByJobunitId", 
						JobCommandParamMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobEnvVariableMstEntity> envVariableList = ((HinemosEntityManager)em).createNamedQuery("JobEnvVariableMstEntity.findByJobunitId", 
						JobEnvVariableMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobNextJobOrderMstEntity> nextJobOrderList = ((HinemosEntityManager)em).createNamedQuery("JobNextJobOrderMstEntity.findByJobunitId", 
						JobNextJobOrderMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				
				// ジョブ変数を保持するマップ <jobunitId, <jobId, List<JobParamMstEntity>>>
				Map<String, Map<String, List<JobParamMstEntity>>> paramMap = new HashMap<String, Map<String, List<JobParamMstEntity>>>();
				// 待ち条件を保持するマップ <jobunitId, <jobId, List<JobWaitGroupMstEntity>>>
				Map<String, Map<String, List<JobWaitGroupMstEntity>>> waitGroupMap = new HashMap<String, Map<String, List<JobWaitGroupMstEntity>>>();
				// 待ち条件を保持するマップ(ターゲットジョブがキー) <targetJobunitId, <targetJobId, List<JobWaitMstEntity>>>
				Map<String, Map<String, List<JobWaitMstEntity>>> waitTargetJobMap = new HashMap<String, Map<String, List<JobWaitMstEntity>>>();
				// ジョブ終了時の変数設定を保持するマップ <jobunitId, <jobId, List<JobCommandParamMstEntity>>>
				Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap = new HashMap<String, Map<String, List<JobCommandParamMstEntity>>>();
				// 環境変数を保持するマップ <jobunitId, <jobId, List<JobEnvVariableMstEntity>>>
				Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap = new HashMap<String, Map<String, List<JobEnvVariableMstEntity>>>();
				// 後続ジョブ実行設定を保持するマップ <jobunitId, <jobId, List<JobNextJobOrderMstEntity>>>
				Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap = new HashMap<String, Map<String, List<JobNextJobOrderMstEntity>>>();
				// 通知グループを保持するマップ <notifyGroupId, List<NotifyRelationInfo>>
				Map<String, List<NotifyRelationInfo>> notifyRelMap = new HashMap<String, List<NotifyRelationInfo>>();
				
				// ジョブ変数マップの作成
				createParamMap(paramList, paramMap);
				
				// 待ち条件マップの作成
				createWaitGroupMap(waitGroupList, waitGroupMap);
				
				// 待ち条件マップの作成(ターゲットジョブがキー)
				createWaitTargetJobMap(waitGroupList, waitTargetJobMap);
				
				// ジョブ終了時の変数設定マップの作成
				createCommandParamMap(commandParamList, commandParamMap);
				
				// 環境変数マップの作成
				createEnvVariableMap(envVariableList, envVariableMap);
				
				// 後続ジョブ実行設定マップの作成
				createNextJobOrderMap(nextJobOrderList, nextJobOrderMap);
				
				// 通知グループマップの作成
				createNotifyRelationMap(notifyRelMap, em.getEntityManager());
				
				List<JobMstEntity> jobs = QueryUtil.getJobMstEnityFindByJobunitId(jobunitId);
				for(JobMstEntity job : jobs) {
					String jobId = job.getId().getJobId();
					try {
						jobunitMstMap.put(jobId, job);
						jobunitInfoMap.put(jobId, createJobInfo(job, paramMap, waitGroupMap, waitTargetJobMap, commandParamMap, envVariableMap, nextJobOrderMap, notifyRelMap));
					} catch (InvalidRole | JobMasterNotFound | HinemosUnknown e) {
						m_log.warn("failed initCache jobunitId=" + jobunitId + " jobId=" + jobId + ". " 
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
					}
				}
				jobMstCache.put(jobunitId, jobunitMstMap);
				jobInfoCache.put(jobunitId, jobunitInfoMap);
				
				storeJobMstCache(jobMstCache);
				storeJobInfoCache(jobInfoCache);
			}
		} finally {
			_lock.writeUnlock();
		}
		m_log.info("updateCache " + (HinemosTime.currentTimeMillis() - start) + "ms");
	}

	/**
	* キャッシュ内を渡って、スコープ名(パス表記)を全て更新する。
	*/
	public static void updateScopesInCache() {
		long start_time = System.currentTimeMillis();

		_lock.writeLock();
		try {
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache == null) {
				m_log.info("updateScopesInCache() : JobInfoCache is null.");
				return;
			}
			for (Map<String, JobInfo> cacheEnrty : jobInfoCache.values()) {
				for (JobInfo job : cacheEnrty.values()) {
					String facilityId = "";	 // for logging
					switch (job.getType()) {
					case JobConstant.TYPE_JOB:
						facilityId = job.getCommand().getFacilityID();
						job.getCommand().setScope(FacilitySelector.getNodeScopePath(null, facilityId));
						break;
					case JobConstant.TYPE_MONITORJOB:
						facilityId = job.getMonitor().getFacilityID();
						job.getMonitor().setScope(FacilitySelector.getNodeScopePath(null,facilityId));
						break;
					case JobConstant.TYPE_FILEJOB:
						facilityId = job.getFile().getSrcFacilityID();
						job.getFile().setSrcScope(FacilitySelector.getNodeScopePath(null,facilityId));
						facilityId = job.getFile().getDestFacilityID();
						job.getFile().setDestScope(FacilitySelector.getNodeScopePath(null,facilityId));
						break;
					case JobConstant.TYPE_JOBLINKRCVJOB:
						facilityId = job.getJobLinkRcv().getFacilityID();
						job.getJobLinkRcv().setScope(FacilitySelector.getNodeScopePath(null,facilityId));
						break;
					case JobConstant.TYPE_FILECHECKJOB:
						facilityId = job.getJobFileCheck().getFacilityID();
						job.getJobFileCheck().setScope(FacilitySelector.getNodeScopePath(null,facilityId));
						break;
					case JobConstant.TYPE_RESOURCEJOB:
						facilityId = job.getResource().getResourceNotifyScope();
						job.getResource().setResourceNotifyScopePath(FacilitySelector.getNodeScopePath(null, facilityId));
						break;
					case JobConstant.TYPE_RPAJOB:
						facilityId = job.getRpa().getFacilityID();
						job.getRpa().setScope(FacilitySelector.getNodeScopePath(null,facilityId));
						break;
					default:
						/* NOP */
						break;
					}
				}
			}

		} finally {
			_lock.writeUnlock();
		}

		m_log.info("updateScopesInCache() : " + (System.currentTimeMillis() - start_time) + "ms.");
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
		// TODO: UserNotFound は投げないように見える。
		// 発見したのは新機能追加作業中のため修正は避けておく。どこかのメンテナンスコミットで削除すべき。
		m_log.trace("createJobData() : " + jobInfo.getJobunitId() + ", " + jobInfo.getId() + "," + jobInfo.isPropertyFull());
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
					m_log.trace("cache hit " + jobunitId + "," + jobId + ", hit=" + jobInfoUnitCache.size());
					return ret;
				}
			} else {
				m_log.trace("cache didn't hit " + jobunitId + "," + jobId);
			}
		} finally {
			_lock.readUnlock();
		}
		
		m_log.trace("createJobData() : " + jobunitId + ", " + jobId);
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
		
		jobInfo = createJobInfo(jobMstEntity, null, null, null, null, null, null, null);
		
		return jobInfo;
	}

	/**
	 * ジョブ情報{@link com.clustercontrol.jobmanagement.bean.JobInfo}を作成します。<BR>
	 *
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @return ジョブ情報
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 */
	public static JobInfo getJobFull(String jobunitId, String jobId) throws HinemosUnknown, JobMasterNotFound, InvalidRole {
		// TODO: UserNotFound は投げないように見える。
		// 発見したのは新機能追加作業中のため修正は避けておく。どこかのメンテナンスコミットで削除すべき。
		m_log.trace("getJobFull() : " + jobunitId + ", " + jobId);
		JobInfo jobInfo = null;
		
		try {
			_lock.readLock();
			
			Map<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			Map<String, JobInfo> jobInfoUnitCache = jobInfoCache.get(jobunitId);
			if (jobInfoUnitCache != null) {
				JobInfo ret = jobInfoUnitCache.get(jobId);
				if (ret != null) {
					m_log.trace("cache hit " + jobunitId + "," + jobId + ", hit=" + jobInfoUnitCache.size());
					return ret;
				}
			} else {
				m_log.trace("cache didn't hit " + jobunitId + "," + jobId);
			}
		} finally {
			_lock.readUnlock();
		}
		
		m_log.trace("createJobData() : " + jobunitId + ", " + jobId);
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
		
		jobInfo = createJobInfo(jobMstEntity, null, null, null, null, null, null, null);
		
		return jobInfo;
	}

	/**
	 * ジョブ情報{@link com.clustercontrol.jobmanagement.bean.JobInfo}を作成します。<BR>
	 * キャッシュからのみ取得します。
	 *
	 * @param jobunitId ジョブユニットID
	 * @param jobId ジョブID
	 * @return ジョブ情報
	 */
	public static JobInfo getJobFullFromCache(String jobunitId, String jobId) {
		m_log.trace("getJobFullFromCache() : " + jobunitId + ", " + jobId);

		try {
			_lock.readLock();

			Map<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			Map<String, JobInfo> jobInfoUnitCache = jobInfoCache.get(jobunitId);
			if (jobInfoUnitCache == null) {
				m_log.trace("cache didn't hit " + jobunitId + "," + jobId);
				return null;
			}
			JobInfo ret = jobInfoUnitCache.get(jobId);
			if (ret == null) {
				m_log.trace("cache didn't hit " + jobunitId + "," + jobId);
			} else {
				m_log.trace("cache hit " + jobunitId + "," + jobId + ", hit=" + jobInfoUnitCache.size());
			}
			return ret;
		} finally {
			_lock.readUnlock();
		}
	}	
	
	/**
	 * ロックし、ローカルからジョブの情報を読み込む。
	 * 該当ジョブユニットのJobMstEntityのリストを返す。
	 * 
	 * @param jobunitId
	 * @param jobId
	 * @return ジョブマスタのリスト
	 */
	public static List<JobMstEntity> getJobMstEntity(String jobunitId) {
		try {
			_lock.readLock();

			Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
			Map<String, JobMstEntity> jobMstUnitCache = jobMstCache.get(jobunitId);
			if (jobMstUnitCache == null) {
				return null;
			}
			return new ArrayList<>(jobMstUnitCache.values());
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ロックし、ローカルからジョブの情報を読み込む。
	 * 該当するものがキャッシュ煮ない場合、DBからは再取得する。
	 * 
	 * @param jobunitId
	 * @param jobId
	 * @return ジョブマスタ
	 * @throws InvalidRole 
	 * @throws JobMasterNotFound 
	 */
	public static JobMstEntity getJobMstEntity(String jobunitId, String jobId) throws JobMasterNotFound, InvalidRole {
		JobMstEntity jobMstEntity = null;
		try {
			_lock.readLock();

			jobMstEntity = getJobMstEntityFromLocal(jobunitId, jobId);
		} finally {
			_lock.readUnlock();
		}
		
		if (jobMstEntity == null) {
			jobMstEntity = QueryUtil.getJobMstPK(jobunitId, jobId);
		}

		return jobMstEntity;
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
	 * @param paramMap
	 */
	private static void setJobParam(JobInfo jobInfo, JobMstEntity jobMstEntity, Map<String, Map<String, List<JobParamMstEntity>>> paramMap) {
		ArrayList<JobParameterInfo> paramList = new ArrayList<JobParameterInfo>();
		
		Collection<JobParamMstEntity> params = null;
		if(paramMap == null) {
			m_log.trace("params get Entities");
			params = jobMstEntity.getJobParamMstEntities();
		} else {
			m_log.trace("params get map");
			if(paramMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				params = paramMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
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
	 * jobMstEntityの情報に基づき、JobNextJobOrderパラメータ関連の情報をjobInfoに設定する。
	 * 新規に追加された後続ジョブの場合、後続ジョブ排他分岐優先度設定をここで作成する。
	 * 新規に追加された後続ジョブの優先度はジョブIDの昇順で既存の優先度設定の後に追加する。
	 * @param jobInfo
	 * @param jobMstEntit
	 * @param waitTargetJobMap
	 * @param nextJobOrderMap
	 */
	private static void setJobNextJobOrder(JobInfo jobInfo, JobMstEntity jobMstEntity, 
			Map<String, Map<String, List<JobWaitMstEntity>>> waitTargetJobMap,
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			ArrayList<JobNextJobOrderInfo> nextJobOrderList = new ArrayList<JobNextJobOrderInfo>();	
			List<JobNextJobOrderMstEntity> orderMstList = null;
			if(nextJobOrderMap == null) {
				m_log.trace("orderMstList get Entities");
				orderMstList = jobMstEntity.getJobNextJobOrderMstEntities();
			} else {
				m_log.trace("orderMstList get map");
				if(nextJobOrderMap.get(jobMstEntity.getId().getJobunitId()) != null) {
					orderMstList = nextJobOrderMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
				}
			}
			
			if(orderMstList != null){
				//優先度順にソート
				//nextJobOrderListに優先度順で登録する
				orderMstList.sort(Comparator.comparing(orderMst -> orderMst.getOrder()));
				for (JobNextJobOrderMstEntity orderMst : orderMstList) {
					JobNextJobOrderInfo orderInfo = new JobNextJobOrderInfo();
					orderInfo.setJobunitId(orderMst.getId().getJobunitId());
					orderInfo.setJobId(orderMst.getId().getJobId());
					orderInfo.setNextJobId(orderMst.getId().getNextJobId());
					nextJobOrderList.add(orderInfo);
				}
			}

			//優先度がまだ未設定の後続ジョブも合わせて取得する
			//優先度設定がない後続ジョブもクライアント側で表示したいため
			List<JobWaitMstEntity> nextJobList = null;
			if(waitTargetJobMap != null) {
				m_log.trace("nextJobList get map");
				if(waitTargetJobMap.get(jobMstEntity.getId().getJobunitId()) != null) {
					nextJobList = waitTargetJobMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
				}
			}
			
			if (nextJobList != null) {
				//セッション横断待ち条件は優先順設定の対象外
				nextJobList.removeIf(
					startJobMst ->
					startJobMst.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS ||
					startJobMst.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE 
				);

				//削除された後続ジョブをリストから除く
				List<String> nextJobIdList = nextJobList
					.stream().map(startJobMst -> startJobMst.getId().getJobId())
					.collect(Collectors.toList());
				nextJobOrderList.removeIf(orderInfo -> !nextJobIdList.contains(orderInfo.getNextJobId()));

				//先行ジョブに新たに追加された後続ジョブの優先度設定を追加する
				//既に優先度が設定されている後続ジョブの後ろに、ジョブIDの昇順で追加する
				//既に優先度設定がある後続ジョブID
				List<String> orderExistsJobIdList = nextJobOrderList.stream()
					.map(nextJobOrder -> nextJobOrder.getNextJobId()).collect(Collectors.toList());
				//後続ジョブのリスト
				nextJobList.stream()
				//優先度が既に設定されている後続ジョブは除外
				.filter(startJobMst -> !orderExistsJobIdList.contains(startJobMst.getId().getJobId()))
				//ジョブIDの昇順にソート
				.sorted(Comparator.comparing(startJobMst -> startJobMst.getId().getJobId()))
				//優先度未設定の後続ジョブに対して優先度設定を新規に作成
				.forEach(startJobMst -> {
					JobNextJobOrderInfo nextJobOrder = new JobNextJobOrderInfo();
					nextJobOrder.setJobunitId(startJobMst.getId().getJobunitId());
					nextJobOrder.setJobId(startJobMst.getId().getTargetJobId());
					nextJobOrder.setNextJobId(startJobMst.getId().getJobId());
					nextJobOrderList.add(nextJobOrder);
				});
			}
			jobInfo.getWaitRule().setExclusiveBranchNextJobOrderList(nextJobOrderList);
		}
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
	private static void setJobNotifications(JobInfo jobInfo, JobMstEntity jobMstEntity, Map<String, List<NotifyRelationInfo>> notifyRelMap)
			throws InvalidRole, HinemosUnknown {
		jobInfo.setBeginPriority(jobMstEntity.getBeginPriority());
		jobInfo.setNormalPriority(jobMstEntity.getNormalPriority());
		jobInfo.setWarnPriority(jobMstEntity.getWarnPriority());
		jobInfo.setAbnormalPriority(jobMstEntity.getAbnormalPriority());

		//通知情報の取得
		List<NotifyRelationInfo> nriList = null;
		if(notifyRelMap == null) {
			m_log.trace("nriList get database");
			nriList = new NotifyControllerBean().getNotifyRelation(jobMstEntity.getNotifyGroupId());
		} else {
			m_log.trace("nriList get map");
			nriList = notifyRelMap.get(jobMstEntity.getNotifyGroupId());
		}
		if (nriList != null) {
			Collections.sort(nriList);
			jobInfo.setNotifyRelationInfos(new ArrayList<NotifyRelationInfo>(nriList));
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
		//ファシリティパスを取得
		fileInfo.setSrcScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getSrcFacilityId()));
		fileInfo.setDestScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getDestFacilityId()));
		jobInfo.setFile(fileInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのコマンド情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @param envVariableMap
	 * @param commandParamMap
	 * @throws HinemosUnknown
	 */
	private static void setJobCommand(JobInfo jobInfo, JobMstEntity jobMstEntity, 
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap,
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap)
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
		commandInfo.setCommandRetryEndStatus(jobMstEntity.getCommandRetryEndStatus());
		// ジョブ終了時のジョブ変数
		ArrayList<JobCommandParam> jobCommandParamList = new ArrayList<>();
		List<JobCommandParamMstEntity> commandParams = null;
		if(commandParamMap == null) {
			m_log.trace("commandParams get Entities");
			commandParams = jobMstEntity.getJobCommandParamEntities();
		} else {
			m_log.trace("commandParams get map");
			if(commandParamMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				commandParams = commandParamMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
		if (commandParams != null && commandParams.size() > 0) {
			for (JobCommandParamMstEntity jobCommandParamEntity : commandParams) {
				if (jobCommandParamEntity != null) {
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
		List<JobEnvVariableMstEntity> envVariables = null;
		if(envVariableMap == null) {
			m_log.trace("envVariables get Entities");
			envVariables = jobMstEntity.getJobEnvVariableMstEntities();
		} else {
			m_log.trace("envVariables get map");
			if(envVariableMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				envVariables = envVariableMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
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

		// 標準出力のファイル出力
		if (jobMstEntity.getJobOutputMstEntities() != null) {
			for (JobOutputMstEntity output : jobMstEntity.getJobOutputMstEntities()) {
				JobOutputInfo outputInfo = new JobOutputInfo();
				outputInfo.setSameNormalFlg(output.getSameNormalFlg());
				outputInfo.setDirectory(output.getDirectory());
				outputInfo.setFileName(output.getFileName());
				outputInfo.setAppendFlg(output.getAppendFlg());
				outputInfo.setFailureOperationFlg(output.getFailureOperationFlg());
				outputInfo.setFailureOperationType(output.getFailureOperationType());
				outputInfo.setFailureOperationEndStatus(output.getFailureOperationEndStatus());
				outputInfo.setFailureOperationEndValue(output.getFailureOperationEndValue());
				outputInfo.setFailureNotifyFlg(output.getFailureNotifyFlg());
				outputInfo.setFailureNotifyPriority(output.getFailureNotifyPriority());
				outputInfo.setValid(output.getValid());
				if (JobOutputType.STDOUT.getCode().equals(output.getId().getOutputType())) {
					commandInfo.setNormalJobOutputInfo(outputInfo);
				} else if (JobOutputType.STDERR.getCode().equals(output.getId().getOutputType())) {
					commandInfo.setErrorJobOutputInfo(outputInfo);
				}
			}
		}

		//ファシリティパスを取得
		commandInfo.setScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
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
		monitorJobInfo.setScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
		monitorJobInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		monitorJobInfo.setMonitorId(jobMstEntity.getMonitorId());
		monitorJobInfo.setMonitorInfoEndValue(jobMstEntity.getMonitorInfoEndValue());
		monitorJobInfo.setMonitorWarnEndValue(jobMstEntity.getMonitorWarnEndValue());
		monitorJobInfo.setMonitorCriticalEndValue(jobMstEntity.getMonitorCriticalEndValue());
		monitorJobInfo.setMonitorUnknownEndValue(jobMstEntity.getMonitorUnknownEndValue());
		monitorJobInfo.setMonitorWaitTime(jobMstEntity.getMonitorWaitTime());
		monitorJobInfo.setMonitorWaitEndValue(jobMstEntity.getMonitorWaitEndValue());
		jobInfo.setMonitor(monitorJobInfo);
	}
	
	/**
	 * jobMstEntityの情報に基づき、JobのRPAシナリオジョブ情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 */
	private static void setRpaJob(JobInfo jobInfo, JobMstEntity jobMstEntity) {
		RpaJobInfo rpaJobInfo = new RpaJobInfo();
		rpaJobInfo.setRpaJobType(jobMstEntity.getRpaJobType());
		// 直接実行
		rpaJobInfo.setFacilityID(jobMstEntity.getFacilityId());
		rpaJobInfo.setScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
		rpaJobInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		rpaJobInfo.setRpaToolId(jobMstEntity.getRpaToolId());
		rpaJobInfo.setRpaExeFilepath(jobMstEntity.getRpaExeFilepath());
		rpaJobInfo.setRpaScenarioFilepath(jobMstEntity.getRpaScenarioFilepath());
		rpaJobInfo.setRpaLogDirectory(jobMstEntity.getRpaLogDirectory());
		rpaJobInfo.setRpaLogFileName(jobMstEntity.getRpaLogFileName());
		rpaJobInfo.setRpaLogEncoding(jobMstEntity.getRpaLogEncoding());
		rpaJobInfo.setRpaLogReturnCode(jobMstEntity.getRpaLogReturnCode());
		rpaJobInfo.setRpaLogPatternHead(jobMstEntity.getRpaLogPatternHead());
		rpaJobInfo.setRpaLogPatternTail(jobMstEntity.getRpaLogPatternTail());
		rpaJobInfo.setRpaLogMaxBytes(jobMstEntity.getRpaLogMaxBytes());
		rpaJobInfo.setRpaDefaultEndValue(jobMstEntity.getRpaDefaultEndValue());
		rpaJobInfo.setRpaLoginFlg(jobMstEntity.getRpaLoginFlg());
		rpaJobInfo.setRpaLoginUserId(jobMstEntity.getRpaLoginUserId());
		rpaJobInfo.setRpaLoginPassword(jobMstEntity.getRpaLoginPassword());
		rpaJobInfo.setRpaLoginRetry(jobMstEntity.getRpaLoginRetry());
		rpaJobInfo.setRpaLoginEndValue(jobMstEntity.getRpaLoginEndValue());
		rpaJobInfo.setRpaLoginResolution(jobMstEntity.getRpaLoginResolution());
		rpaJobInfo.setRpaLogoutFlg(jobMstEntity.getRpaLogoutFlg());
		rpaJobInfo.setRpaScreenshotEndDelayFlg(jobMstEntity.getRpaScreenshotEndDelayFlg());
		rpaJobInfo.setRpaScreenshotEndValueFlg(jobMstEntity.getRpaScreenshotEndValueFlg());
		rpaJobInfo.setRpaScreenshotEndValue(jobMstEntity.getRpaScreenshotEndValue());
		rpaJobInfo.setRpaScreenshotEndValueCondition(jobMstEntity.getRpaScreenshotEndValueCondition());
		rpaJobInfo.setRpaNotLoginNotify(jobMstEntity.getRpaNotLoginNotify());
		rpaJobInfo.setRpaNotLoginNotifyPriority(jobMstEntity.getRpaNotLoginNotifyPriority());
		rpaJobInfo.setRpaNotLoginEndValue(jobMstEntity.getRpaNotLoginEndValue());
		rpaJobInfo.setRpaAlreadyRunningNotify(jobMstEntity.getRpaAlreadyRunningNotify());
		rpaJobInfo.setRpaAlreadyRunningNotifyPriority(jobMstEntity.getRpaAlreadyRunningNotifyPriority());
		rpaJobInfo.setRpaAlreadyRunningEndValue(jobMstEntity.getRpaAlreadyRunningEndValue());
		rpaJobInfo.setRpaAbnormalExitNotify(jobMstEntity.getRpaAbnormalExitNotify());
		rpaJobInfo.setRpaAbnormalExitNotifyPriority(jobMstEntity.getRpaAbnormalExitNotifyPriority());
		rpaJobInfo.setRpaAbnormalExitEndValue(jobMstEntity.getRpaAbnormalExitEndValue());
		rpaJobInfo.setRpaJobOptionInfos(new ArrayList<>());
		jobMstEntity.getJobRpaOptionMstEntities().forEach( e -> {
			RpaJobOptionInfo optionInfo = new RpaJobOptionInfo();
			optionInfo.setOrderNo(e.getId().getOrderNo());
			optionInfo.setOption(e.getOption());
			optionInfo.setDescription(e.getDescription());
			rpaJobInfo.getRpaJobOptionInfos().add(optionInfo);
		}); 
		rpaJobInfo.setRpaJobEndValueConditionInfos(new ArrayList<>());
		jobMstEntity.getJobRpaEndValueConditionMstEntities().forEach( e -> {
			RpaJobEndValueConditionInfo endValueInfo = new RpaJobEndValueConditionInfo();
			endValueInfo.setOrderNo(e.getId().getOrderNo());
			endValueInfo.setConditionType(e.getConditionType());
			endValueInfo.setPattern(e.getPattern());
			endValueInfo.setCaseSensitivityFlg(e.getCaseSensitivityFlg());
			endValueInfo.setProcessType(e.getProcessType());
			endValueInfo.setReturnCode(e.getReturnCode());
			endValueInfo.setReturnCodeCondition(e.getReturnCodeCondition());
			endValueInfo.setUseCommandReturnCodeFlg(e.getUseCommandReturnCodeFlg());
			endValueInfo.setEndValue(e.getEndValue());
			endValueInfo.setDescription(e.getDescription());
			rpaJobInfo.getRpaJobEndValueConditionInfos().add(endValueInfo);
		});
		// 間接実行
		rpaJobInfo.setRpaScopeId(jobMstEntity.getRpaScopeId());
		rpaJobInfo.setRpaRunType(jobMstEntity.getRpaRunType());
		rpaJobInfo.setRpaScenarioParam(jobMstEntity.getRpaScenarioParam());
		rpaJobInfo.setRpaStopType(jobMstEntity.getRpaStopType());
		rpaJobInfo.setRpaStopMode(jobMstEntity.getRpaStopMode());
		rpaJobInfo.setRpaRunConnectTimeout(jobMstEntity.getRpaRunConnectTimeout());
		rpaJobInfo.setRpaRunRequestTimeout(jobMstEntity.getRpaRunRequestTimeout());
		rpaJobInfo.setRpaRunEndFlg(jobMstEntity.getRpaRunEndFlg());
		rpaJobInfo.setRpaRunRetry(jobMstEntity.getRpaRunRetry());
		rpaJobInfo.setRpaRunEndValue(jobMstEntity.getRpaRunEndValue());
		rpaJobInfo.setRpaCheckConnectTimeout(jobMstEntity.getRpaCheckConnectTimeout());
		rpaJobInfo.setRpaCheckRequestTimeout(jobMstEntity.getRpaCheckRequestTimeout());
		rpaJobInfo.setRpaCheckEndFlg(jobMstEntity.getRpaCheckEndFlg());
		rpaJobInfo.setRpaCheckRetry(jobMstEntity.getRpaCheckRetry());
		rpaJobInfo.setRpaCheckEndValue(jobMstEntity.getRpaCheckEndValue());
		rpaJobInfo.setRpaJobRunParamInfos(new ArrayList<>());
		jobMstEntity.getJobRpaRunParamMstEntities().forEach(e -> {
			RpaJobRunParamInfo runParamInfo = new RpaJobRunParamInfo();
			runParamInfo.setParamId(e.getId().getParamId());
			runParamInfo.setParamValue(e.getParamValue());
			rpaJobInfo.getRpaJobRunParamInfos().add(runParamInfo);
		});
		rpaJobInfo.setRpaJobCheckEndValueInfos(new ArrayList<>());
		jobMstEntity.getJobRpaCheckEndValueMstEntities().forEach(e -> {
			RpaJobCheckEndValueInfo checkEndValueInfo = new RpaJobCheckEndValueInfo();
			checkEndValueInfo.setEndStatusId(e.getId().getEndStatusId());
			checkEndValueInfo.setEndValue(e.getEndValue());
			rpaJobInfo.getRpaJobCheckEndValueInfos().add(checkEndValueInfo);
		});
		// 直接実行・間接実行共通
		rpaJobInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		rpaJobInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		rpaJobInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		rpaJobInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		rpaJobInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		rpaJobInfo.setCommandRetryEndStatus(jobMstEntity.getCommandRetryEndStatus());
		jobInfo.setRpa(rpaJobInfo);
		
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのジョブ連携送信ジョブ情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setJobLinkSendJob(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobLinkSendInfo jobLinkSendInfo = new JobLinkSendInfo();
		jobLinkSendInfo.setRetryFlg(jobMstEntity.getRetryFlg());
		jobLinkSendInfo.setRetryCount(jobMstEntity.getRetryCount());
		jobLinkSendInfo.setFailureOperation(jobMstEntity.getFailureOperation());
		jobLinkSendInfo.setFailureEndStatus(jobMstEntity.getFailureEndStatus());
		jobLinkSendInfo.setJoblinkMessageId(jobMstEntity.getJoblinkMessageId());
		jobLinkSendInfo.setPriority(jobMstEntity.getPriority());
		jobLinkSendInfo.setMessage(jobMstEntity.getMessage());
		jobLinkSendInfo.setSuccessEndValue(jobMstEntity.getSuccessEndValue());
		jobLinkSendInfo.setFailureEndValue(jobMstEntity.getFailureEndValue());
		jobLinkSendInfo.setJoblinkSendSettingId(jobMstEntity.getJoblinkSendSettingId());
		if (jobMstEntity.getJobLinkJobExpMstEntities() != null) {
			jobLinkSendInfo.setJobLinkExpList(new ArrayList<>());
			for (JobLinkJobExpMstEntity entity : jobMstEntity.getJobLinkJobExpMstEntities()) {
				JobLinkExpInfo info = new JobLinkExpInfo();
				info.setKey(entity.getId().getKey());
				info.setValue(entity.getValue());
				jobLinkSendInfo.getJobLinkExpList().add(info);
			}
		}
		jobInfo.setJobLinkSend(jobLinkSendInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのジョブ連携待機ジョブ情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setJobLinkRcvJob(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobLinkRcvInfo jobLinkRcvInfo = new JobLinkRcvInfo();
		jobLinkRcvInfo.setFacilityID(jobMstEntity.getFacilityId());
		//ファシリティパスを取得
		jobLinkRcvInfo.setScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
		jobLinkRcvInfo.setMonitorInfoEndValue(jobMstEntity.getMonitorInfoEndValue());
		jobLinkRcvInfo.setMonitorWarnEndValue(jobMstEntity.getMonitorWarnEndValue());
		jobLinkRcvInfo.setMonitorCriticalEndValue(jobMstEntity.getMonitorCriticalEndValue());
		jobLinkRcvInfo.setMonitorUnknownEndValue(jobMstEntity.getMonitorUnknownEndValue());
		jobLinkRcvInfo.setFailureEndFlg(jobMstEntity.getFailureEndFlg());
		jobLinkRcvInfo.setMonitorWaitTime(jobMstEntity.getMonitorWaitTime());
		jobLinkRcvInfo.setMonitorWaitEndValue(jobMstEntity.getMonitorWaitEndValue());
		jobLinkRcvInfo.setJoblinkMessageId(jobMstEntity.getJoblinkMessageId());
		jobLinkRcvInfo.setMessage(jobMstEntity.getMessage());
		jobLinkRcvInfo.setPastFlg(jobMstEntity.getPastFlg());
		jobLinkRcvInfo.setPastMin(jobMstEntity.getPastMin());
		jobLinkRcvInfo.setInfoValidFlg(jobMstEntity.getInfoValidFlg());
		jobLinkRcvInfo.setWarnValidFlg(jobMstEntity.getWarnValidFlg());
		jobLinkRcvInfo.setCriticalValidFlg(jobMstEntity.getCriticalValidFlg());
		jobLinkRcvInfo.setUnknownValidFlg(jobMstEntity.getUnknownValidFlg());
		jobLinkRcvInfo.setApplicationFlg(jobMstEntity.getApplicationFlg());
		jobLinkRcvInfo.setApplication(jobMstEntity.getApplication());
		jobLinkRcvInfo.setMonitorDetailIdFlg(jobMstEntity.getMonitorDetailIdFlg());
		jobLinkRcvInfo.setMonitorDetailId(jobMstEntity.getMonitorDetailId());
		jobLinkRcvInfo.setMessageFlg(jobMstEntity.getMessageFlg());
		jobLinkRcvInfo.setExpFlg(jobMstEntity.getExpFlg());
		jobLinkRcvInfo.setMonitorAllEndValueFlg(jobMstEntity.getMonitorAllEndValueFlg());
		jobLinkRcvInfo.setMonitorAllEndValue(jobMstEntity.getMonitorAllEndValue());
		if (jobMstEntity.getJobLinkJobExpMstEntities() != null) {
			jobLinkRcvInfo.setJobLinkExpList(new ArrayList<>());
			for (JobLinkJobExpMstEntity entity : jobMstEntity.getJobLinkJobExpMstEntities()) {
				JobLinkExpInfo info = new JobLinkExpInfo();
				info.setKey(entity.getId().getKey());
				info.setValue(entity.getValue());
				jobLinkRcvInfo.getJobLinkExpList().add(info);
			}
		}
		if (jobMstEntity.getJobLinkInheritMstEntities() != null) {
			jobLinkRcvInfo.setJobLinkInheritList(new ArrayList<>());
			for (JobLinkInheritMstEntity entity : jobMstEntity.getJobLinkInheritMstEntities()) {
				JobLinkInheritInfo info = new JobLinkInheritInfo();
				info.setParamId(entity.getId().getParamId());
				info.setKeyInfo(entity.getKeyInfo());
				info.setExpKey(entity.getExpKey());
				jobLinkRcvInfo.getJobLinkInheritList().add(info);
			}
		}
		jobInfo.setJobLinkRcv(jobLinkRcvInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのファイルチェックジョブ情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setFileCheckJob(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobFileCheckInfo jobFileCheckInfo = new JobFileCheckInfo();
		jobFileCheckInfo.setFacilityID(jobMstEntity.getFacilityId());
		//ファシリティパスを取得
		jobFileCheckInfo.setScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
		jobFileCheckInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		jobFileCheckInfo.setSuccessEndValue(jobMstEntity.getSuccessEndValue());
		jobFileCheckInfo.setFailureEndFlg(jobMstEntity.getFailureEndFlg());
		jobFileCheckInfo.setFailureWaitTime(jobMstEntity.getFailureWaitTime());
		jobFileCheckInfo.setFailureEndValue(jobMstEntity.getFailureEndValue());
		jobFileCheckInfo.setDirectory(jobMstEntity.getDirectory());
		jobFileCheckInfo.setFileName(jobMstEntity.getFileName());
		jobFileCheckInfo.setCreateValidFlg(jobMstEntity.getCreateValidFlg());
		jobFileCheckInfo.setCreateBeforeJobStartFlg(jobMstEntity.getCreateBeforeJobStartFlg());
		jobFileCheckInfo.setDeleteValidFlg(jobMstEntity.getDeleteValidFlg());
		jobFileCheckInfo.setModifyValidFlg(jobMstEntity.getModifyValidFlg());
		jobFileCheckInfo.setModifyType(jobMstEntity.getModifyType());
		jobFileCheckInfo.setNotJudgeFileInUseFlg(jobMstEntity.getNotJudgeFileInUseFlg());
		jobFileCheckInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		jobFileCheckInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		jobFileCheckInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		jobInfo.setJobFileCheck(jobFileCheckInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobのリソース制御ジョブ情報をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 */
	private static void setResourceJob(JobInfo jobInfo, JobMstEntity jobMstEntity) {
		ResourceJobInfo resourceJobInfo = new ResourceJobInfo();
		resourceJobInfo.setResourceType(jobMstEntity.getResourceType());
		resourceJobInfo.setResourceCloudScopeId(jobMstEntity.getResourceCloudScopeId());
		resourceJobInfo.setResourceLocationId(jobMstEntity.getResourceLocationId());
		resourceJobInfo.setResourceTargetId(jobMstEntity.getResourceTargetId());
		resourceJobInfo.setResourceAction(jobMstEntity.getResourceAction());
		resourceJobInfo.setResourceStatusConfirmTime(jobMstEntity.getResourceStatusConfirmTime());
		resourceJobInfo.setResourceStatusConfirmInterval(jobMstEntity.getResourceStatusConfirmInterval());
		resourceJobInfo.setResourceAttachNode(jobMstEntity.getResourceAttachNode());
		resourceJobInfo.setResourceAttachDevice(jobMstEntity.getResourceAttachDevice());
		resourceJobInfo.setResourceNotifyScope(jobMstEntity.getFacilityId());
		// ファシリティパスを取得
		resourceJobInfo.setResourceNotifyScopePath(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
		resourceJobInfo.setResourceSuccessValue(jobMstEntity.getResourceSuccessValue());
		resourceJobInfo.setResourceFailureValue(jobMstEntity.getResourceFailureValue());
		jobInfo.setResource(resourceJobInfo);
	}

	/**
	 * jobMstEntityの情報に基づき、Jobの待ち条件をjobInfoに設定する。
	 * @param jobInfo
	 * @param jobMstEntity
	 * @param waitGroupMap
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private static void setJobWaitRule(JobInfo jobInfo, JobMstEntity jobMstEntity, 
			Map<String, Map<String, List<JobWaitGroupMstEntity>>> waitGroupMap)
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
				jobMstEntity.getJobType() == JobConstant.TYPE_MONITORJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_FILECHECKJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_RESOURCEJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_RPAJOB){
			waitRule.setSuspend(jobMstEntity.getSuspend());
			waitRule.setCondition(jobMstEntity.getConditionType());
			waitRule.setEndCondition(jobMstEntity.getUnmatchEndFlg());
			waitRule.setEndStatus(jobMstEntity.getUnmatchEndStatus());
			waitRule.setEndValue(jobMstEntity.getUnmatchEndValue());
			waitRule.setSkip(jobMstEntity.getSkip());
			waitRule.setSkipEndStatus(jobMstEntity.getSkipEndStatus());
			waitRule.setSkipEndValue(jobMstEntity.getSkipEndValue());
			waitRule.setExclusiveBranch(jobMstEntity.getExclusiveBranchFlg());
			waitRule.setExclusiveBranchEndStatus(jobMstEntity.getExclusiveBranchEndStatus());
			waitRule.setExclusiveBranchEndValue(jobMstEntity.getExclusiveBranchEndValue());
			waitRule.setCalendar(jobMstEntity.getCalendar());
			waitRule.setCalendarId(jobMstEntity.getCalendarId());
			waitRule.setCalendarEndStatus(jobMstEntity.getCalendarEndStatus());
			waitRule.setCalendarEndValue(jobMstEntity.getCalendarEndValue());
			waitRule.setJobRetryFlg(jobMstEntity.getJobRetryFlg());
			waitRule.setJobRetry(jobMstEntity.getJobRetry());
			waitRule.setJobRetryInterval(jobMstEntity.getJobRetryInterval());
			waitRule.setJobRetryEndStatus(jobMstEntity.getJobRetryEndStatus());

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
			waitRule.setEnd_delay_change_mount(jobMstEntity.getEndDelayChangeMount());
			waitRule.setEnd_delay_change_mount_value(jobMstEntity.getEndDelayChangeMountValue());
			waitRule.setMultiplicityNotify(jobMstEntity.getMultiplicityNotify());
			waitRule.setMultiplicityNotifyPriority(jobMstEntity.getMultiplicityNotifyPriority());
			waitRule.setMultiplicityOperation(jobMstEntity.getMultiplicityOperation());
			waitRule.setMultiplicityEndValue(jobMstEntity.getMultiplicityEndValue());
			waitRule.setQueueFlg(jobMstEntity.getQueueFlg());
			waitRule.setQueueId(jobMstEntity.getQueueId());
		}

		//待ち条件を取得
		Collection<JobWaitGroupMstEntity> waitGroupList = null;
		if(waitGroupMap == null) {
			m_log.trace("waitGroupList get Entities");
			waitGroupList = jobMstEntity.getJobWaitGroupMstEntities();
		} else {
			m_log.trace("waitGroupList get map");
			if(waitGroupMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				waitGroupList = waitGroupMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
		ArrayList<JobObjectGroupInfo> objectGroupList = new ArrayList<>();
		if(waitGroupList != null && waitGroupList.size() > 0){
			for (JobWaitGroupMstEntity waitGroup : waitGroupList){
				if(waitGroup == null){
					continue;
				}
				JobObjectGroupInfo objectGroupInfo = new JobObjectGroupInfo();
				objectGroupInfo.setOrderNo(waitGroup.getId().getOrderNo());
				objectGroupInfo.setConditionType(waitGroup.getConditionType());
				objectGroupInfo.setIsGroup(waitGroup.getIsGroup());
				ArrayList<JobObjectInfo> objectList = new ArrayList<>();
				for (JobWaitMstEntity wait : waitGroup.getJobWaitMstEntities()) {
					JobObjectInfo objectInfo = new JobObjectInfo();
					if (wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_TIME
							&& wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_START_MINUTE
							&& wait.getId().getTargetJobType() != JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
						// ジョブ名格納用
						String jobName = "";
						//対象ジョブを取得
						Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCacheWithoutDebugLog();
						if (jobMstCache.get(wait.getId().getTargetJobunitId()) != null
								&& jobMstCache.get(wait.getId().getTargetJobunitId()).get(wait.getId().getTargetJobId()) != null) {
							// キャッシュが存在し、ジョブ名が取得できるならキャッシュから取得
							JobMstEntity targetJob = jobMstCache.get(wait.getId().getTargetJobunitId()).get(wait.getId().getTargetJobId());
							jobName = targetJob.getJobName();
						} else {
							// ジョブ名をSQLで取得する
							try {
								JobMstEntity targetJob= QueryUtil.getJobMstPK(wait.getId().getTargetJobunitId(), wait.getId().getTargetJobId());
								jobName = targetJob.getJobName();
							} catch(JobMasterNotFound e) {
								m_log.error("targetJob Not Found : " + e.getMessage());
								jobName = "";
							}
						}
						objectInfo.setJobId(wait.getId().getTargetJobId());
						objectInfo.setJobName(jobName);
					}
					objectInfo.setType(wait.getId().getTargetJobType());
					switch (objectInfo.getType()) {
					case JudgmentObjectConstant.TYPE_JOB_END_STATUS:
						/** ジョブ終了状態 */
						objectInfo.setStatus(wait.getId().getTargetInt1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_END_VALUE:
						/** ジョブ終了値 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					case JudgmentObjectConstant.TYPE_TIME:
						/** 時刻 */
						objectInfo.setTime(wait.getId().getTargetLong());
						break;
					case JudgmentObjectConstant.TYPE_START_MINUTE:
						/** セッション開始時の時間（分）  */
						objectInfo.setStartMinute(wait.getId().getTargetInt1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_PARAMETER:
						/** ジョブ変数 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setDecisionValue(wait.getId().getTargetStr1());
						objectInfo.setValue(wait.getId().getTargetStr2());
						break;
					case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS:
						/** セッション横断ジョブ終了状態 */
						objectInfo.setStatus(wait.getId().getTargetInt1());
						objectInfo.setCrossSessionRange(wait.getId().getTargetInt2());
						break;
					case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE:
						/** セッション横断ジョブ終了値 */
						objectInfo.setCrossSessionRange(wait.getId().getTargetInt2());
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					case JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE:
						/** ジョブ戻り値 */
						objectInfo.setDecisionCondition(wait.getId().getTargetInt1());
						objectInfo.setValue(wait.getId().getTargetStr1());
						break;
					default:
						break;
					}
					objectInfo.setDescription(wait.getDescription());
					objectList.add(objectInfo);
				}
				objectGroupInfo.setJobObjectList(objectList);
				objectGroupList.add(objectGroupInfo);
			}
		}
		/*
		 * ソート処理
		 */
		Collections.sort(objectGroupList);
		waitRule.setObjectGroup(objectGroupList);
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
		if (jobInfo.isPropertyFull() != null && !jobInfo.isPropertyFull()) {
			JobInfo oldJob = getJobFull(jobInfo);
			JobInfo clonedOldJob = null;
			try {
				clonedOldJob = (JobInfo) oldJob.cloneDeepWaitRuleOnly();
			} catch (Exception e) {
				m_log.warn("setJobTreeFull() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}
			if (jobInfo.isWaitRuleChanged() && jobInfo.getWaitRule() != null) {
				// 待ち条件が更新されている場合
				m_log.info("Job wait rule changed with propertyFull=false for jobId=" + job.getData().getId());
				JobWaitRuleInfo newWaitRule = jobInfo.getWaitRule();
				clonedOldJob.getWaitRule()
						.setExclusiveBranchNextJobOrderList(newWaitRule.getExclusiveBranchNextJobOrderList());
				clonedOldJob.getWaitRule().setObjectGroup(newWaitRule.getObjectGroup());
			}
			if (jobInfo.isReferJobChanged()) {
				// 参照ジョブ情報が更新されている場合
				m_log.info("Job reference changed with propertyFull=false for jobId=" + job.getData().getId());
				String newReferJobUnitId = jobInfo.getReferJobUnitId();
				String newReferJobId = jobInfo.getReferJobId();
				Integer newReferSelectType = jobInfo.getReferJobSelectType();
				clonedOldJob.setReferJobUnitId(newReferJobUnitId);
				clonedOldJob.setReferJobId(newReferJobId);
				clonedOldJob.setReferJobSelectType(newReferSelectType);
			}
			job.setData(clonedOldJob);
		}
		if (job.getChildren() == null) {
			return;
		}
		for (JobTreeItem childJob : job.getChildren()) {
			setJobTreeFull(childJob);
		}
	}

	/**
	 * キャッシュ中に指定の通知IDが含まれていないか検索して
	 * 含まれていたなら、そのジョブ定義のキャッシュを更新する。
	 * 
	 * @param findNotifyIds 検索したい通知ID(配列)
	 */
	public static void updateCacheForNotifyId(String[] findNotifyIds) {
		final long start = HinemosTime.currentTimeMillis();
		if (m_log.isDebugEnabled()) {
			m_log.debug("updateCacheForNotifyId() id=" + String.join(",", findNotifyIds));
		}
		//アップデート対象ユニットを取得
		//  キャッシュを走査して 通知IDをジョブ定義に用いてるジョブユニットを探し出す。
		//  NotifyRelationInfoから紐づくジョブ定義の取得は、
		//  データ構造的に考慮外なので（通知グループIDの文字列中にIDが埋込まれているのみ）キャッシュの走査処理で代替。
		final Set<String> updateJobUnitIdSet = getContainJobUnitFromCache(findNotifyIds);
		
		//対象ジョブユニットのキャッシュを更新
		for (String unitId : updateJobUnitIdSet) {
			updateCache(unitId);
		}
		m_log.info("updateCacheForNotifyId() " + (HinemosTime.currentTimeMillis() - start) + "ms");
	}
	
	// マップがnullでない場合はジョブ変数、待ち条件、ジョブ終了時の変数設定、環境変数、待ち条件(ジョブ変数)を引数のマップから取得する
	private static JobInfo createJobInfo(JobMstEntity jobMstEntity,
			Map<String, Map<String, List<JobParamMstEntity>>> paramMap,
			Map<String, Map<String, List<JobWaitGroupMstEntity>>> waitGroupMap,
			Map<String, Map<String, List<JobWaitMstEntity>>> waitTargetJobMap,
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap,
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap,
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap,
			Map<String, List<NotifyRelationInfo>> notifyRelMap) 
					throws InvalidRole, JobMasterNotFound, HinemosUnknown {
		JobInfo jobInfo = new JobInfo(jobMstEntity.getId().getJobunitId(), jobMstEntity.getId().getJobId(), jobMstEntity.getJobName(), jobMstEntity.getJobType());
		
		jobInfo.setDescription(jobMstEntity.getDescription());
		jobInfo.setIconId(jobMstEntity.getIconId());
		if (jobMstEntity.getJobType() == JobConstant.TYPE_JOBUNIT) {
			// jobMstEntityではジョブユニットの値のみ使用する
			jobInfo.setExpNodeRuntimeFlg(jobMstEntity.getExpNodeRuntimeFlg());
		}
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

		setJobWaitRule(jobInfo, jobMstEntity, waitGroupMap);
		setJobNextJobOrder(jobInfo, jobMstEntity, waitTargetJobMap, nextJobOrderMap);

		switch (jobMstEntity.getJobType()) {
		case JobConstant.TYPE_JOB:
			setJobCommand(jobInfo, jobMstEntity, envVariableMap, commandParamMap);
			break;
		case JobConstant.TYPE_FILEJOB:
			setJobFile(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_JOBUNIT:
			setJobParam(jobInfo, jobMstEntity, paramMap);
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
		case JobConstant.TYPE_JOBLINKSENDJOB:
			setJobLinkSendJob(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_JOBLINKRCVJOB:
			setJobLinkRcvJob(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_FILECHECKJOB:
			setFileCheckJob(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_RESOURCEJOB:
			setResourceJob(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_RPAJOB:
			setRpaJob(jobInfo, jobMstEntity);
			break;
		default:
			break;
		}
		if (jobInfo.getType() != JobConstant.TYPE_REFERJOB && jobInfo.getType() != JobConstant.TYPE_REFERJOBNET) {
			setJobNotifications(jobInfo, jobMstEntity, notifyRelMap);
			setJobEndStatus(jobInfo, jobMstEntity);
		}

		jobInfo.setPropertyFull(true);
		return jobInfo;
	}
	
	private static void createParamMap(List<JobParamMstEntity> paramList, 
			Map<String, Map<String, List<JobParamMstEntity>>> paramMap) {
		
		for(JobParamMstEntity param : paramList) {
			String jobunitId = param.getId().getJobunitId();
			String jobId = param.getId().getJobId();
			
			Map<String, List<JobParamMstEntity>> map = paramMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobParamMstEntity>>();
			}
			
			List<JobParamMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobParamMstEntity>();
			}
		
			list.add(param);
			map.put(jobId, list);
			paramMap.put(jobunitId, map);
		}
		m_log.debug("paramMap size=" + paramMap.size());
	}
	
	private static void createWaitGroupMap(List<JobWaitGroupMstEntity> waitGroupList, 
			Map<String, Map<String, List<JobWaitGroupMstEntity>>> waitGroupMap) {
		
		for(JobWaitGroupMstEntity waitGroup : waitGroupList) {
			String jobunitId = waitGroup.getId().getJobunitId();
			String jobId = waitGroup.getId().getJobId();
			
			Map<String, List<JobWaitGroupMstEntity>> map = waitGroupMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobWaitGroupMstEntity>>();
			}
			
			List<JobWaitGroupMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobWaitGroupMstEntity>();
			}
		
			list.add(waitGroup);
			map.put(jobId, list);
			waitGroupMap.put(jobunitId, map);
		}
		m_log.debug("waitGroupMap size=" + waitGroupMap.size());
	}
	
	private static void createWaitTargetJobMap(List<JobWaitGroupMstEntity> waitGroupList, 
			Map<String, Map<String, List<JobWaitMstEntity>>> waitJobTargetJobMap) {
		
		for(JobWaitGroupMstEntity waitGroup : waitGroupList) {
			if (waitGroup.getJobWaitMstEntities() == null) {
				continue;
			}
			for (JobWaitMstEntity wait : waitGroup.getJobWaitMstEntities()) {
				String targetJobunitId = wait.getId().getTargetJobunitId();
				String targetJobId = wait.getId().getTargetJobId();
				
				Map<String, List<JobWaitMstEntity>> map = waitJobTargetJobMap.get(targetJobunitId);
				if(map == null) {
					map = new HashMap<String, List<JobWaitMstEntity>>();
				}
				
				List<JobWaitMstEntity> list = map.get(targetJobId);
				if(list == null) {
					list = new ArrayList<JobWaitMstEntity>();
				}
			
				list.add(wait);
				map.put(targetJobId, list);
				waitJobTargetJobMap.put(targetJobunitId, map);
			}
		}
		m_log.debug("startJobTargetJobMap size=" + waitJobTargetJobMap.size());
	}

	private static void createCommandParamMap(List<JobCommandParamMstEntity> commandParamList, 
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap) {
		
		for(JobCommandParamMstEntity commandParam : commandParamList) {
			String jobunitId = commandParam.getId().getJobunitId();
			String jobId = commandParam.getId().getJobId();
			
			Map<String, List<JobCommandParamMstEntity>> map = commandParamMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobCommandParamMstEntity>>();
			}
			
			List<JobCommandParamMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobCommandParamMstEntity>();
			}
		
			list.add(commandParam);
			map.put(jobId, list);
			commandParamMap.put(jobunitId, map);
		}
		m_log.debug("commandParamMap size=" + commandParamMap.size());
	}

	private static void createEnvVariableMap(List<JobEnvVariableMstEntity> envVariableList, 
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap) {
		
		for(JobEnvVariableMstEntity envVariable : envVariableList) {
			String jobunitId = envVariable.getId().getJobunitId();
			String jobId = envVariable.getId().getJobId();
			
			Map<String, List<JobEnvVariableMstEntity>> map = envVariableMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobEnvVariableMstEntity>>();
			}
			
			List<JobEnvVariableMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobEnvVariableMstEntity>();
			}
		
			list.add(envVariable);
			map.put(jobId, list);
			envVariableMap.put(jobunitId, map);
		}
		m_log.debug("envVariableMap size=" + envVariableMap.size());
	}
	
	private static void createNextJobOrderMap(List<JobNextJobOrderMstEntity> nextJobOrderList,
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap) {
		
		for(JobNextJobOrderMstEntity nextJobOrder : nextJobOrderList) {
			String jobunitId = nextJobOrder.getId().getJobunitId();
			String jobId = nextJobOrder.getId().getJobId();
			
			Map<String, List<JobNextJobOrderMstEntity>> map = nextJobOrderMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobNextJobOrderMstEntity>>();
			}
			
			List<JobNextJobOrderMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobNextJobOrderMstEntity>();
			}
		
			list.add(nextJobOrder);
			map.put(jobId, list);
			nextJobOrderMap.put(jobunitId, map);
		}
		m_log.debug("nextJobOrderMap size=" + nextJobOrderMap.size());
	}
	
	private static void createNotifyRelationMap(Map<String, List<NotifyRelationInfo>> notifyRelMap, EntityManager em) {
		List<NotifyRelationInfo> notifyRelList = com.clustercontrol.notify.util.QueryUtil.getNotifyRelationInfoJob();
		if (notifyRelList != null) {
			for(NotifyRelationInfo info : notifyRelList) {
				// EntityManagerの管理対象から除外する
				em.detach(info);
				String notifyGroupId = info.getNotifyGroupId();
				
				List<NotifyRelationInfo> list = notifyRelMap.get(notifyGroupId);
				if(list == null) {
					list = new ArrayList<NotifyRelationInfo>();
				}
				list.add(info);
				notifyRelMap.put(notifyGroupId, list);
			}
		}
		m_log.debug("notifyRelMap size=" + notifyRelMap.size());
	}
	
	/**
	 * JobInfoキャッシュ中に指定の通知IDが含まれていないか検索して
	 * 該当するジョブ定義を保持するジョブユニットのIDを返却
	 * 
	 * @param findNotifyIds 検索したい通知ID(配列)
	 * @return ジョブユニットのID(セット)
	 */
	private static Set<String> getContainJobUnitFromCache(String[] findNotifyIds) {
		
		final List<String> notifyIdList = Arrays.asList(findNotifyIds);
		final Set<String> updateIDSet = new HashSet<String>();
		
		try {
			_lock.readLock();
			//キャッシュ内のユニット毎にチェック処理
			Map<String, Map<String, JobInfo>> jobInfoCache = getJobInfoCache();
			for ( Map.Entry<String,Map<String,JobInfo>> unitEntry : jobInfoCache.entrySet()) {
				//ユニット毎の全ジョブ定義について、指定の通知IDが含まれていないか検索
				Map<String, JobInfo> unitInfoMap = unitEntry.getValue();
				boolean isNeedUpdate = false;
				if (m_log.isDebugEnabled()) {
					m_log.debug("getContainJobUnitFromCache() searchUnitId=" + unitEntry.getKey());
				}
				for (JobInfo jobInfo : unitInfoMap.values()) {
					if (jobInfo == null || jobInfo.getNotifyRelationInfos() == null) {
						continue;
					}
					for (NotifyRelationInfo relationInfo : jobInfo.getNotifyRelationInfos()) {
						if (relationInfo == null || relationInfo.getNotifyId() == null) {
							continue;
						}
						if (m_log.isTraceEnabled()) {
							m_log.trace("getContainJobUnitFromCache() jobInfo.getId=" + jobInfo.getId() + ", relationInfo.getNotifyId=" + relationInfo.getNotifyId());
						}
						if (notifyIdList.contains(relationInfo.getNotifyId())) {
							//指定の通知IDが含まれていたら 該当ユニットをリストに追加
							isNeedUpdate = true;
							break;
						}
					}
					if (isNeedUpdate) {
						updateIDSet.add(unitEntry.getKey());
						break;
					}
				}
			}
		} finally {
			_lock.readUnlock();
		}
		return updateIDSet;
		
	}
}
