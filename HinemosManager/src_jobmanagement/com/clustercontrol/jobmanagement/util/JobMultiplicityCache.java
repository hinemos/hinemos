/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.RetryWaitStatusConstant;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.model.JobWaitGroupInfoEntity;
import com.clustercontrol.jobmanagement.model.JobWaitInfoEntity;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;


/**
 * ジョブの多重度のキャッシュを保持するクラス<br/>
 * 
 * キャッシュには以下があり、
 * ジョブが起動すると、実行予定キャッシュ→待ちキャッシュ→実行中キャッシュに登録される。
 * <ul>
 * <li>実行予定キャッシュ</li>
 * <li>待ちキャッシュ</li>
 * <li>実行中キャッシュ</li>
 * </ul>
 * なお、実行予定キャッシュは、実行するノードに割振るために使用される。
 */
public class JobMultiplicityCache {
	private static Log m_log = LogFactory.getLog( JobMultiplicityCache.class );

	// map操作やqueue操作の際にはこのロックを利用する。
	private static final ILock _lock;

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(JobMultiplicityCache.class.getName());
		
		try {
			_lock.writeLock();
			
			Map<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			if (waitingCache == null) {	// not null when clustered
				storeWaitingCache(new HashMap<String, Queue<JobSessionNodeEntityPK>>());
			}
		} catch (Throwable e) {
			m_log.error("static " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Queue<JobSessionNodeEntityPK>> getWaitingCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_WAITING);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_WAITING + " : " + cache);
		return cache == null ? null : (HashMap<String, Queue<JobSessionNodeEntityPK>>)cache;
	}
	
	private static void storeWaitingCache(HashMap<String, Queue<JobSessionNodeEntityPK>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_WAITING + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_WAITING, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Queue<JobSessionNodeEntityPK>> getRunningCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_RUNNING);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_RUNNING + " : " + cache);
		return cache == null ? null : (HashMap<String, Queue<JobSessionNodeEntityPK>>)cache;
	}
	
	private static void storeRunningCache(HashMap<String, Queue<JobSessionNodeEntityPK>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_RUNNING + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_RUNNING, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Queue<JobSessionNodeEntityPK>> getGoingToRunCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_GOING);
		if (m_log.isDebugEnabled()) m_log.debug("getGoingToRunCache() get cache " + AbstractCacheManager.KEY_JOB_GOING + " : " + cache);
		return cache == null ? null : (HashMap<String, Queue<JobSessionNodeEntityPK>>)cache;
	}
	
	private static void storeGoingToRunCache(HashMap<String, Queue<JobSessionNodeEntityPK>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("storeGoingToRunCache() store cache " + AbstractCacheManager.KEY_JOB_GOING + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_GOING, newCache);
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, Queue<JobSessionNodeEntityPK>> getRunningRpaCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_RUNNING_RPA);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_RUNNING_RPA + " : " + cache);
		return cache == null ? null : (HashMap<String, Queue<JobSessionNodeEntityPK>>)cache;
	}
	
	private static void storeRunningRpaCache(HashMap<String, Queue<JobSessionNodeEntityPK>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_RUNNING_RPA + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_RUNNING_RPA, newCache);
	}
	
	/**
	 * ノードごとの現在の多重度を返すメソッド。
	 * リポジトリ[エージェント]ビューから呼ばれる。
	 * @param facilityId
	 * @return
	 */
	public static Integer getRunningMultiplicity(String facilityId) {
		try {
			_lock.readLock();
			
			Map<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			Map<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
			return (runningQueue == null ? 0 : runningQueue.size()) + (runningRpaQueue == null ? 0 : runningRpaQueue.size());
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ノードごとの現在の待ちジョブ数を返すメソッド。
	 * リポジトリ[エージェント]ビューから呼ばれる。
	 * @param facilityId
	 * @return
	 */
	public static Integer getWaitMultiplicity(String facilityId) {
		try {
			_lock.readLock();
			
			Map<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			Queue<JobSessionNodeEntityPK> waitingQueue = waitingCache.get(facilityId);
			return waitingQueue == null ? 0 : waitingQueue.size();
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ノードごとの現在の実行予定数を返すメソッド。
	 * @param facilityId
	 * @return
	 */
	public static Integer getGoingToRunMultiplicity(String facilityId) {
		m_log.debug("getGoingToRunMultiplicity(), facilityId: " + facilityId);
		
		try {
			_lock.readLock();

			Map<String, Queue<JobSessionNodeEntityPK>> cache = getGoingToRunCache();
			Queue<JobSessionNodeEntityPK> queue = null;
			if (cache != null) {
				queue = cache.get(facilityId);
			}
			m_log.debug("GoingToRunMultiplicity(), GoingToRunQueue: " + (queue == null ? "null" : queue));

			return queue == null ? 0 : queue.size();
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ノードごとの現在の、以下のジョブ数の合計を返すメソッド。
	 * ジョブ実行数
	 * ジョブ待ち数
	 * ジョブ実行予定数
	 * 
	 * @param facilityId
	 * @return
	 */
	public static int getMultiplicity(String facilityId) {
		m_log.debug("getMultiplicity(), facilityId: " + facilityId);

		try {
			_lock.readLock();

			// ジョブ実行数
			Queue<JobSessionNodeEntityPK> runningQueue = getRunningCache().get(facilityId);
			int running = runningQueue == null ? 0 : runningQueue.size();

			// 待ちジョブ数
			Queue<JobSessionNodeEntityPK> waitingQueue = getWaitingCache().get(facilityId);
			int waiting = waitingQueue == null ? 0 : waitingQueue.size();

			// 実行予定数
			Map<String, Queue<JobSessionNodeEntityPK>> goingCache = getGoingToRunCache();
			int going = 0;
			if (goingCache != null) {
				Queue<JobSessionNodeEntityPK> goingQueue = goingCache.get(facilityId);
				going = goingQueue == null ? 0 : goingQueue.size();
			}

			m_log.debug("running job: " + running + ", waiting job: " + waiting + ", scheduled job: " + going);
			return running + waiting + going;
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ジョブ多重度を確認して、ジョブが実行できるか確認するメソッド
	 * @param facilityId
	 * @return
	 */
	public static boolean isRunNow(String facilityId) {
		int multiplicity = 0;
		int queueSize = 0;
		
		try {
			RepositoryControllerBean repositoryControllerBean = new RepositoryControllerBean();
			// facilityIdがスコープのものであれば多重度を考慮しない
			if(!repositoryControllerBean.isNode(facilityId)){
				return true;
			}
			NodeInfo nodeInfo = repositoryControllerBean.getNode(facilityId);
			multiplicity = nodeInfo.getJobMultiplicity();
		} catch (FacilityNotFound e) {
			m_log.warn("kick " + e.getMessage());
		} catch (HinemosUnknown e) {
			m_log.warn("kick " + e.getMessage(),e);
		}
		
		if (multiplicity == 0) {
			// ジョブ多重度が0以下に設定されていた場合は、多重度のロジックは利用しない。
			return true;
		}
		
		try {
			_lock.readLock();
			
			Map<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			Map<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
			queueSize = (runningQueue == null ? 0 : runningQueue.size()) + (runningRpaQueue == null ? 0 : runningRpaQueue.size());

			if(m_log.isDebugEnabled()){
				m_log.debug("isRunNow runningQueue : " + runningQueue);
				m_log.debug("isRunNow runningRpaQueue : " + runningRpaQueue);
			}
			
			return queueSize < multiplicity;
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ジョブ多重度とセッションの実行状態を確認して、ジョブが実行できるか確認するメソッド
	 * checkMultiplicityからのみ呼ばれる。エラーが起きたら上位もtrueで返却しているのでtrueとする。
	 * @param JobSessionNodeEntity
	 * @return
	 */
	public static boolean isRunNowWithSession(JobSessionJobEntity sessionJob, String facilityId) {
		int multiplicity = 0;
		int queueSize = 0;

		try {
			RepositoryControllerBean repositoryControllerBean = new RepositoryControllerBean();
			// facilityIdがスコープのものであれば多重度を考慮しない
			if(!repositoryControllerBean.isNode(facilityId)){
				return true;
			}
			NodeInfo nodeInfo = repositoryControllerBean.getNode(facilityId);
			multiplicity = nodeInfo.getJobMultiplicity();
		} catch (FacilityNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
		} catch (HinemosUnknown e) {
			m_log.warn("wait2running " + e.getMessage(),e);
		}

		if (multiplicity == 0) {
			// ジョブ多重度が0以下に設定されていた場合は、多重度のロジックは利用しない。
			return true;
		}

		try {
			_lock.readLock();

			Map<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			Map<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
			queueSize = (runningQueue == null ? 0 : runningQueue.size()) + (runningRpaQueue == null ? 0 : runningRpaQueue.size());
			
			// runningQueueが空の場合は多重度のロジックは利用しない。
			if(queueSize == 0){
				m_log.debug("runningQueue is null or runningQueue.size()=0");
				return true;
			}

			// 待ち条件の先行ジョブに自分自身のノードがあり、runningQueueに存在する場合はqueueSizeから差っ引くため、判定を行う
			if (!sessionJob.getJobInfoEntity().getJobWaitGroupInfoEntities().isEmpty()) {
				//待ち条件ジョブを取得
				JobSessionJobEntity targetSessionJob;
				for (JobWaitGroupInfoEntity waitGroup : sessionJob.getJobInfoEntity().getJobWaitGroupInfoEntities()) {
					// ループ1：待ち条件数
					for (JobWaitInfoEntity wait : waitGroup.getJobWaitInfoEntities()) {
						// セッション横断ジョブは除外
						if (wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS 
								|| wait.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE
								|| wait.getTargetJobId() == null){
							continue;
						}

						// セッション情報を取得
						targetSessionJob = QueryUtil.getJobSessionJobPK(
								sessionJob.getId().getSessionId(),
								wait.getTargetJobunitId(),
								wait.getTargetJobId());

						// 待ち条件の対象となるジョブの実行状態を確認し、runningCacheに存在している場合は除外する。
						if (StatusConstant.isEndGroup(targetSessionJob.getStatus())) {

							if(m_log.isDebugEnabled()){
								m_log.debug("TargetJob finished. Check if TargetJob exists in runningQueue");
							}

							// ループ2：先行ジョブのセッションノード
							for (JobSessionNodeEntity targetNode : targetSessionJob.getJobSessionNodeEntities()) {
								if ((runningQueue != null && runningQueue.contains(targetNode.getId()))
										|| (runningRpaQueue != null && runningRpaQueue.contains(targetNode.getId()))) {

									m_log.info("TargetJob : " + targetNode.getId().getJobId()
											+ " found in runningQueue. Excluding runningQueue size.");

									if(m_log.isDebugEnabled()){
										m_log.debug("Before runningQueue size : " + queueSize 
												+ ", Target seesionId : " + targetNode.getId().getSessionId());
									}
									// runningQueueとentityで実行状態のラグがあるため、除算する
									queueSize--;
								}
							}
						}
					}
				}
			}

			if(m_log.isDebugEnabled()){
				m_log.debug("isRunNowWithSession runningQueue : " + runningQueue);
				m_log.debug("isRunNowWithSession runningRpaQueue : " + runningRpaQueue);
			}

			return queueSize < multiplicity;
		} catch (JobInfoNotFound e) {
			m_log.warn("wait2running " + e.getMessage());
		} catch (InvalidRole e) {
			m_log.warn("wait2running " + e.getMessage());
		} finally {
			_lock.readUnlock();
		}
		return true;
	}

	/**
	 * statusが100(StatusConstant.TYPE_RUNNING)に遷移したい場合は、このメソッドを呼ぶこと。
	 *
	 * このメソッドはwaitQueueに該当のジョブを追加する。
	 * また、waitQueueに追加したジョブを実行するために、kickを呼び出す。
	 * （多重度が多い場合はwaitQueueのまま待機。
	 *   多重度が少ない場合はwaitQueueから削除され、runningQueueに追加。）
	 * @param facilityId
	 */
	public static boolean toRunning(JobSessionNodeEntityPK pk) {
		m_log.info("toRunning " + pk);

		String facilityId = pk.getFacilityId();
		
		try {
			_lock.writeLock();
			
			HashMap<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			HashMap<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			HashMap<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			
			Queue<JobSessionNodeEntityPK> waitingQueue = waitingCache.get(facilityId);
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
			
			if (waitingQueue == null) {
				waitingQueue = new LinkedList<JobSessionNodeEntityPK>();
				waitingCache.put(facilityId, waitingQueue);
			}
			
			if (runningQueue == null) {
				runningQueue = new LinkedList<JobSessionNodeEntityPK>();
				runningCache.put(facilityId, runningQueue);
			}
			
			if (runningRpaQueue == null) {
				runningRpaQueue = new LinkedList<JobSessionNodeEntityPK>();
				runningRpaCache.put(facilityId, runningRpaQueue);
			}
			
			if (! runningQueue.contains(pk) 
					&& ! runningRpaQueue.contains(pk)
					&& ! waitingQueue.contains(pk)) {
				m_log.debug("toRunning add waitQueue : " + pk);
				waitingQueue.offer(pk);
			}

			storeWaitingCache(waitingCache);
			storeRunningCache(runningCache);
			storeRunningRpaCache(runningRpaCache);

			if(m_log.isDebugEnabled()){
				for(JobSessionNodeEntityPK q : runningQueue){
					m_log.debug("toRunning runningQueue : " + q);
				}
				for(JobSessionNodeEntityPK q : runningRpaQueue){
					m_log.debug("toRunning runningRpaQueue : " + q);
				}
				for(JobSessionNodeEntityPK q : waitingQueue){
					m_log.debug("toRunning waitQueue : " + q);
				}
			}

			// 数行上でwaitQueueに追加されたジョブを実行。
			kick(facilityId);
		} finally {
			_lock.writeUnlock();
		}
		return true;
	}

	/**
	 * statusが100(StatusConstant.TYPE_RUNNING)から別の状態に遷移したら、このメソッドを呼ぶこと。
	 *
	 * このメソッドはrunningQueueから該当のジョブを削除して、多重度を下げる。
	 * また、多重度を下げた後に、待っているジョブを実行させる。（kick）
	 * @param pk
	 */
	public static boolean fromRunning(JobSessionNodeEntityPK pk) {
		m_log.info("fromRunning " + pk);

		String facilityId = pk.getFacilityId();
		
		try {
			_lock.writeLock();
			
			HashMap<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			HashMap<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
			if (runningQueue == null && runningRpaQueue == null) {
				m_log.warn("fromRunning " + pk);
				return false;
			}
			
			if (!isRpaJobDirect(pk)) {
				if (runningQueue.remove(pk)) { //// runningQueueから削除
					storeRunningCache(runningCache);
				} else {
					// 普通は実行中から停止に遷移するが、
					// 実行中以外(待機など)から、停止に遷移することがある。
					m_log.info("fromRunning(). from not-running to stop : " + pk);
				}
			} else {
				if (runningRpaQueue.remove(pk)) { //// runningRpaQueueから削除
					storeRunningRpaCache(runningRpaCache);
				} else {
					m_log.info("fromRunning(). from not-running to stop : " + pk);
				}
			}
			
			if(m_log.isDebugEnabled()){
				if (runningQueue != null) {
					for(JobSessionNodeEntityPK q : runningQueue){
						m_log.debug("fromRunning runningQueue : " + q);
					}
				}
				if (runningRpaQueue != null) {
					for(JobSessionNodeEntityPK q : runningRpaQueue){
						m_log.debug("fromRunning runningRpaQueue : " + q);
					}
				}
			}

			// 同一facilityIDのノードで待機中のジョブを実行状態に遷移。
			kick(facilityId);
		} catch (JobInfoNotFound | InvalidRole e) {
			m_log.warn("fromRunning() : " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
		}
		return true;
	}

	/**
	 * waitQueueから削除する。
	 *
	 * ジョブ詳細で実行中で、多重度が多く待機になっている場合は、
	 * waitQueueからジョブを削除する必要がある。
	 *
	 * ジョブ詳細が待機で、ノード詳細も待機の場合はwaitQueueから削除する必要はない。
	 * （しかし、念のためこのメソッドを呼ぶこと。）
	 *
	 * @param pk
	 */
	public static void removeWait(JobSessionNodeEntityPK pk) {
		try {
			_lock.writeLock();
			
			HashMap<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			
			Queue<JobSessionNodeEntityPK> waitingQueue = waitingCache.get(pk.getFacilityId());
			if (waitingQueue != null) {
				if (waitingQueue.remove(pk)) {
					m_log.info("removeWait " + pk);
					storeWaitingCache(waitingCache);
				}
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 実行予定キャッシュに設定する
	 * @param pk
	 * @return
	 */
	public static boolean storeGoingToRun(JobSessionNodeEntityPK pk) {
		m_log.debug("storeGoingToRun(): " + (pk == null ? "null" : pk));
		if (pk == null) {
			//findbugs対応 nullで呼び出されることはあり得ない前提だがfindbugs向けにチェックを設定
			return false;
		}
		try {
			_lock.writeLock();

			HashMap<String, Queue<JobSessionNodeEntityPK>> cache = getGoingToRunCache();
			if (cache == null) {
				m_log.debug("GoingToRunCache is null");
				cache = new HashMap<String, Queue<JobSessionNodeEntityPK>>();
			}
			Queue<JobSessionNodeEntityPK> queue = cache.get(pk.getFacilityId());
			if (queue == null) {
				m_log.debug("GoingToRunQueue is null, FacilityId: " + pk.getFacilityId());
				queue = new LinkedList<JobSessionNodeEntityPK>();
				cache.put(pk.getFacilityId(), queue);
			}
			queue.add(pk);
			storeGoingToRunCache(cache);
			m_log.debug("stored. GoingToRunCache: " + cache);
		} finally {
			_lock.writeUnlock();
		}

		return true;
	}

	/**
	 * 実行予定キャッシュから削除する
	 * @param pk
	 * @return
	 */
	public static boolean removeGoingToRun(JobSessionNodeEntityPK pk) {
		m_log.debug("removeGoingToRun(): " + (pk == null ? "null" : pk));

		try {
			_lock.writeLock();

			HashMap<String, Queue<JobSessionNodeEntityPK>> cache = getGoingToRunCache();
			if (cache == null) {
				m_log.debug("GoingToRunCache is null");
				return false;
			}
			Queue<JobSessionNodeEntityPK> queue = cache.get(pk.getFacilityId());
			if (queue == null) {
				m_log.debug("GoingToRunQueue is null, FacilityId: " + pk.getFacilityId());
				return false;
			}
			if (!queue.remove(pk)) {
				m_log.debug("NOT removed: " + pk);
				return false;
			}
			storeGoingToRunCache(cache);
			m_log.debug("removed. GoingToRunCache: " + cache);
		} finally {
			_lock.writeUnlock();
		}

		return true;
	}

	/**
	 * 実行予定キャッシュから実行中のジョブセッション以外を削除する。
	 * @param sessionIdList
	 */
	public static void removeGoingToRunExceptSessionList(List<String> sessionIdList) {
		m_log.debug("removeGoingToRunExceptSessionList()");

		try {
			_lock.writeLock();

			Map<String, Queue<JobSessionNodeEntityPK>> goingCache = getGoingToRunCache();
			if (goingCache == null) {
				return;
			}
			HashMap<String, Queue<JobSessionNodeEntityPK>> newCache = new HashMap<>();
			//findbugs対応  keySet から entrySetに変更
			for (Entry<String, Queue<JobSessionNodeEntityPK>> entrySet : goingCache.entrySet()) {
				String facilityId = entrySet.getKey();
				Queue<JobSessionNodeEntityPK> goingQueue = entrySet.getValue();
				if (goingQueue == null) {
					m_log.debug("GoingToRunQueue is null, facilityId: " + facilityId);
					continue;
				}
				Queue<JobSessionNodeEntityPK> newQueue = new LinkedList<>();

				for (String sessionId : sessionIdList) {
					for (JobSessionNodeEntityPK pk : goingQueue) {
						if (!pk.getSessionId().equals(sessionId)) {
							continue;
						}
						// session id が一致するものは残すため、設定
						newQueue.add(pk);
					}
				}
				newCache.put(facilityId, newQueue);
			}

			m_log.debug("removed cache except session list. GoingToRunCache: " + newCache);
			storeGoingToRunCache(newCache);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * ノード詳細のジョブを実行する。
	 * ただし、ジョブ多重度が上限に達している場合は、実行されない。
	 *
	 * @param facilityId
	 * @return
	 */
	public static void kick(String facilityId) {
		m_log.debug("kick " + facilityId);

		boolean kickFlag = false;
		
		try {
			_lock.writeLock();
			
			HashMap<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			HashMap<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			HashMap<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			
			Queue<JobSessionNodeEntityPK> waitingQueue = waitingCache.get(facilityId);
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
			
			if (waitingQueue == null || waitingQueue.size() == 0) {
				return;
			}
			
			if (runningQueue == null) {
				runningQueue = new LinkedList<JobSessionNodeEntityPK>();
				runningCache.put(facilityId, runningQueue);
			}

			if (runningRpaQueue == null) {
				runningRpaQueue = new LinkedList<JobSessionNodeEntityPK>();
				runningRpaCache.put(facilityId, runningRpaQueue);
			}
			
			if (isRunNow(facilityId)) {
				JpaTransactionManager jtm = new JpaTransactionManager();
				try {
					jtm.begin();
					JobSessionNodeEntityPK pk = waitingQueue.peek(); //// waitQueueから取得(まだ削除はしない)
					// RPAシナリオジョブ（直接実行）の場合は多重度に依らず複数同時実行しない
					if (isRpaJobDirect(pk) && runningRpaQueue.size() > 0) {
						m_log.debug("rpa job is already running : " + pk);
						// 次に待っているRPAシナリオジョブ（直接実行）以外のジョブを探す
						for (JobSessionNodeEntityPK e : waitingQueue) {
							if (!isRpaJobDirect(e)) {
								m_log.debug("next runnable job : " + pk);
								pk = e;
								break;
							}
						}
						if (isRpaJobDirect(pk)) {
							m_log.debug("next runnable job not found");
							pk = null;  // RPAシナリオジョブ（直接実行）以外の実行すべきジョブが無い
						}
					}
					if (pk != null) {
						m_log.debug("kick remove waitQueue : " + pk);
						int status = new JobSessionNodeImpl().wait2running(pk);
						// ジョブノードを実行済み
						if (status == 0) {
							waitingQueue.remove(pk); //// waitQueueから削除(先頭でない場合もあるためremoveする)
							if (!isRpaJobDirect(pk)) {
								m_log.debug("kick add runningQueue : " + pk);
								runningQueue.offer(pk); //// runningQueueに追加
							} else {
								m_log.debug("kick add runningRpaQueue : " + pk);
								runningRpaQueue.offer(pk); //// runningRpaQueueに追加
							}
							kickFlag = true;
						}
						// ジョブノードを実行しない（ジョブ詳細の実行状態が「実行中」ではない）
						else if (status == 1) {
							m_log.debug("kick not add runningQueue : " + pk);
							waitingQueue.remove(pk); //// waitQueueから削除(先頭でない場合もあるためremoveする)
							kickFlag = true;
						}
					}
					jtm.commit();
				} catch (RuntimeException e) {
					//findbugs対応 RuntimeException のキャッチを明示化
					m_log.warn("kick : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
				} catch (Exception e) {
					m_log.warn("kick : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
				} finally {
					jtm.close();
				}
			}
			
			storeWaitingCache(waitingCache);
			storeRunningCache(runningCache);
			storeRunningRpaCache(runningRpaCache);
			
			if(m_log.isDebugEnabled()){
				for(JobSessionNodeEntityPK q : runningQueue){
					m_log.debug("kick runningQueue : " + q);
				}
				for(JobSessionNodeEntityPK q : runningRpaQueue){
					m_log.debug("kick runningRpaQueue : " + q);
				}
				for(JobSessionNodeEntityPK q : waitingQueue){
					m_log.debug("kick waitQueue : " + q);
				}
			}
			
			if (kickFlag) {
				kick(facilityId);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 「マネージャ起動時」と「ジョブ履歴削除」から呼ばれる。
	 *
	 * 処理概要は下記の通り。
	 *
	 * 処理1.
	 * ノード詳細からrunningを検索して、runningQueueを構築する。
	 *
	 * 処理2.
	 * ジョブ詳細からrunnningを検索して、ジョブを実行する。JobSessionNodeImpl().startNode()
	 * 正確に言うと、startNodeメソッドの内部で待機中のジョブに対して、toRunningさせる。
	 * (toRunningによりwaitQueueに追加され、多重度が低ければ、kickメソッド内部で実行中に遷移する。)
	 */
	public static synchronized void refresh() {
		List<JobSessionJobEntityPK> execJobList = new ArrayList<JobSessionJobEntityPK>();
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transaction has not been begined.");
				return;
			}
			m_log.info("cache refresh start");
			long start = System.currentTimeMillis();
			long time1, time2, time3;
			
			try {
				_lock.writeLock();
				
				HashMap<String, Queue<JobSessionNodeEntityPK>> runningCache = new HashMap<String, Queue<JobSessionNodeEntityPK>>();
				HashMap<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = new HashMap<String, Queue<JobSessionNodeEntityPK>>();
				storeWaitingCache(new HashMap<String, Queue<JobSessionNodeEntityPK>>());
				
				// runningQueueの再構築
				{
					// オブジェクト権限チェックなし
					List<JobSessionNodeEntity> nodeList = em.createNamedQuery("JobSessionNodeEntity.findByStatus", JobSessionNodeEntity.class, ObjectPrivilegeMode.NONE)
							.setParameter("status", StatusConstant.TYPE_RUNNING).getResultList();
					for (JobSessionNodeEntity node : nodeList) {
						String facilityId = node.getId().getFacilityId();
						Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
						Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
						if (isWaitingJobSession(node.getId())) {
							m_log.debug("session job is retry waiting : " + node.getId());
							continue;
						}
						if (!isRpaJobDirect(node.getId())) {
							if (runningQueue == null) {
								runningQueue = new LinkedList<JobSessionNodeEntityPK>();
								runningCache.put(facilityId, runningQueue);
							}
							m_log.debug("refresh add runningQueue : " + node.getId());
							runningQueue.offer(node.getId());
						} else {
							if (runningRpaQueue == null) {
								runningRpaQueue = new LinkedList<JobSessionNodeEntityPK>();
								runningRpaCache.put(facilityId, runningRpaQueue);
							}
							m_log.debug("refresh add runningRpaQueue : " + node.getId());
							runningRpaQueue.offer(node.getId());
						}
					}
				}
				
				storeRunningCache(runningCache);
				storeRunningRpaCache(runningRpaCache);
			} catch (JobInfoNotFound | InvalidRole e) {
				m_log.warn("refresh() : " + e.getMessage(), e);
			} finally {
				_lock.writeUnlock();
			}
			time1 = System.currentTimeMillis() - start;
			start = System.currentTimeMillis();

			// 履歴削除により多重度が下がったノードを実行させるlistを作成
			{
				// オブジェクト権限チェックなし
				List<JobSessionJobEntity> jobList = em.createNamedQuery("JobSessionJobEntity.findByStatus", JobSessionJobEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("status", StatusConstant.TYPE_RUNNING).getResultList();
				for (JobSessionJobEntity job : jobList) {
					if (job.getJobInfoEntity() == null || job.getJobInfoEntity().getJobType() == null) {
						m_log.info("wait job is deleted"); // 待機中のジョブが履歴削除により消された場合にこのルートを通る。
						continue;
					}
					if (job.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOB
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_APPROVALJOB
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_MONITORJOB
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBLINKSENDJOB
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBLINKRCVJOB
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_FILECHECKJOB
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_RESOURCEJOB
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_RPAJOB) {
						continue;
					}
					execJobList.add(job.getId());
				}
			}
			time2 = System.currentTimeMillis() - start;
			start = System.currentTimeMillis();

			// execJobListで実行
			for (JobSessionJobEntityPK id : execJobList) {
				try {
					m_log.info("refresh() startNode=" + id);
					new JobSessionNodeImpl().startNode(id.getSessionId(), id.getJobunitId(), id.getJobId(), false);
				} catch (InvalidRole e) {
					m_log.warn("refresh " + e.getMessage());
				} catch (JobInfoNotFound e) {
					m_log.warn("refresh " + e.getMessage());
				} catch (HinemosUnknown e) {
					m_log.warn("refresh " + e.getMessage());
				}
			}
			time3 = System.currentTimeMillis() - start;
			m_log.info("cache refresh end " + time1 + "+" + time2 + "+" + time3 + "ms");
			print();
		}
	}

	private static void print() {
		try {
			_lock.readLock();
			
			Map<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			for (Map.Entry<String, Queue<JobSessionNodeEntityPK>> facilityIdEntry : waitingCache.entrySet()) {
				m_log.info("print facilityId=" + facilityIdEntry.getKey() + ", waitQueue=" + facilityIdEntry.getValue().size());
			}
		} finally {
			_lock.readUnlock();
		}
		
		try {
			_lock.readLock();
			
			Map<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			for (Map.Entry<String, Queue<JobSessionNodeEntityPK>> facilityIdEntry : runningCache.entrySet()) {
				m_log.info("print facilityId=" + facilityIdEntry.getKey() + ", runningQueue=" + facilityIdEntry.getValue().size());
			}
		} finally {
			_lock.readUnlock();
		}
		
		try {
			_lock.readLock();
			
			Map<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			for (Map.Entry<String, Queue<JobSessionNodeEntityPK>> facilityIdEntry : runningRpaCache.entrySet()) {
				m_log.info("print facilityId=" + facilityIdEntry.getKey() + ", runningRpaQueue=" + facilityIdEntry.getValue().size());
			}
		} finally {
			_lock.readUnlock();
		}
	}

	public static String getJobQueueStr() {
		StringBuilder message = new StringBuilder();
		try {
			_lock.readLock();

			Map<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
			Map<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = getRunningRpaCache();
			message.append("Running:");
			if( null != runningCache ){
				message.append('\n');
				
				for (Map.Entry<String, Queue<JobSessionNodeEntityPK>> facilityIdEntry : runningCache.entrySet()) {
					Queue<JobSessionNodeEntityPK> queue = facilityIdEntry.getValue();
					
					StringBuilder str = new StringBuilder();
					for (JobSessionNodeEntityPK pk : queue) {
						str.append("[" + pk.getSessionId() + "," + pk.getJobunitId() + "," + pk.getJobId() + "]\n");
					}
					message.append(facilityIdEntry.getKey() + "(" + queue.size() + ")=\n" + str.toString());
				}
			}else{
				message.append(" null\n");
			}
			message.append("RunningRpa:");
			if( null != runningRpaCache ){
				message.append('\n');
				
				for (Map.Entry<String, Queue<JobSessionNodeEntityPK>> facilityIdEntry : runningRpaCache.entrySet()) {
					Queue<JobSessionNodeEntityPK> queue = facilityIdEntry.getValue();
					
					StringBuilder str = new StringBuilder();
					for (JobSessionNodeEntityPK pk : queue) {
						str.append("[" + pk.getSessionId() + "," + pk.getJobunitId() + "," + pk.getJobId() + "]\n");
					}
					message.append(facilityIdEntry.getKey() + "(" + queue.size() + ")=\n" + str.toString());
				}
			}else{
				message.append(" null\n");
			}
		} finally {
			_lock.readUnlock();
		}

		try {
			_lock.readLock();

			Map<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			message.append("Wait:\n");
			for (Map.Entry<String, Queue<JobSessionNodeEntityPK>> facilityIdEntry : waitingCache.entrySet()) {
				Queue<JobSessionNodeEntityPK> queue = facilityIdEntry.getValue();
				StringBuilder str = new StringBuilder();
				for (JobSessionNodeEntityPK pk : queue) {
					str.append("[" + pk.getSessionId() + "," + pk.getJobunitId() + "," + pk.getJobId() + "]\n");
				}
				message.append(facilityIdEntry.getKey() + "(" + queue.size() + ")=\n" + str);
			}
		} finally {
			_lock.readUnlock();
		}

		return message.toString();
	}

	/**
	 * 「先行ジョブが終了したタイミングのみ」呼ばれる。
	 * 
	 * 処理概要は下記の通り。
	 * ノード詳細からrunningを検索して、runningQueueを構築する。
	 *
	 */
	public static synchronized void refreshRunningQueue() {
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			m_log.info("RunningQueue refresh start");
			long start = System.currentTimeMillis();
			long time;
			
			try {
				_lock.writeLock();
				
				HashMap<String, Queue<JobSessionNodeEntityPK>> runningCache = new HashMap<String, Queue<JobSessionNodeEntityPK>>();
				HashMap<String, Queue<JobSessionNodeEntityPK>> runningRpaCache = new HashMap<String, Queue<JobSessionNodeEntityPK>>();
				
				try {
					jtm.begin();
					// runningQueueの再構築
					{
						// オブジェクト権限チェックなし
						List<JobSessionNodeEntity> nodeList = em.createNamedQuery("JobSessionNodeEntity.findByStatus", JobSessionNodeEntity.class, ObjectPrivilegeMode.NONE)
								.setParameter("status", StatusConstant.TYPE_RUNNING).getResultList();
						for (JobSessionNodeEntity node : nodeList) {
							String facilityId = node.getId().getFacilityId();
						Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
						Queue<JobSessionNodeEntityPK> runningRpaQueue = runningRpaCache.get(facilityId);
						if (isWaitingJobSession(node.getId())) {
							m_log.debug("session job is retry waiting : " + node.getId());
							continue;
						}
						if (!isRpaJobDirect(node.getId())) {
							if (runningQueue == null) {
								runningQueue = new LinkedList<JobSessionNodeEntityPK>();
								runningCache.put(facilityId, runningQueue);
							}
							m_log.debug("refresh add runningQueue : " + node.getId());
							runningQueue.offer(node.getId());
						} else {
							if (runningRpaQueue == null) {
								runningRpaQueue = new LinkedList<JobSessionNodeEntityPK>();
								runningRpaCache.put(facilityId, runningRpaQueue);
							}
							m_log.debug("refresh add runningRpaQueue : " + node.getId());
							runningRpaQueue.offer(node.getId());
						}
						}
					}
					jtm.commit();
				} catch (Exception e) {
					m_log.warn("RunningQueue refresh : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
				} finally {
					jtm.close();
				}
				
				storeRunningCache(runningCache);
				storeRunningRpaCache(runningRpaCache);
			} finally {
				_lock.writeUnlock();
			}
			time = System.currentTimeMillis() - start;
			m_log.info("RunningQueue refresh end " + time + "ms");
		}
	}
		
	/**
	 * waitingキューに指定したジョブが存在する場合は true、存在しない場合は false を返します。
	 */
	public static boolean existsInWaitingQueue(JobSessionNodeEntityPK pk) {
		try {
			_lock.readLock();

			Map<String, Queue<JobSessionNodeEntityPK>> waitingCache = getWaitingCache();
			if (waitingCache == null) return false;
			
			Queue<JobSessionNodeEntityPK> queue = waitingCache.get(pk.getFacilityId());
			if (queue == null) return false;

			for (JobSessionNodeEntityPK it : queue) {
				if (pk.equals(it)) return true;
			}
			return false;
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ジョブ種別がRPAシナリオジョブ（直接実行）かどうかを返します。
	 * @param pk
	 * @return true : RPAシナリオジョブ（直接実行）、false : それ以外
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	private static boolean isRpaJobDirect(JobSessionNodeEntityPK pk) throws JobInfoNotFound, InvalidRole {
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(pk.getSessionId(),
				pk.getJobunitId(), pk.getJobId());
		JobInfoEntity job = sessionJob.getJobInfoEntity();
		return job.getJobType() == JobConstant.TYPE_RPAJOB
				&& job.getRpaJobType() == RpaJobTypeConstant.DIRECT;
	}

	/**
	 * 親セッションジョブが繰り返し待機中かどうかを返します。
	 * @param pk
	 * @return true:繰り返し待機中、false:それ以外の状態
	 */
	private static boolean isWaitingJobSession(JobSessionNodeEntityPK pk) throws JobInfoNotFound, InvalidRole {
		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(pk.getSessionId(), pk.getJobunitId(),
				pk.getJobId());
		return sessionJob.getRetryWaitStatus() == RetryWaitStatusConstant.WAIT
				|| sessionJob.getRetryWaitStatus() == RetryWaitStatusConstant.PARENT_WAIT;
	}
}
