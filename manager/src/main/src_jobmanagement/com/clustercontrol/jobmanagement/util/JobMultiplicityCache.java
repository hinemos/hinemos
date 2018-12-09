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
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

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
			return runningQueue == null ? 0 : runningQueue.size();
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ノードごとの現在の多重度を返すメソッド。
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
	 * ジョブ多重度を確認して、ジョブが実行できるか確認するメソッド
	 * @param facilityId
	 * @return
	 */
	public static boolean isRunNow(String facilityId) {
		int multiplicity = 0;
		int queueSize = 0;
		
		try {
			NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
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
			queueSize = runningQueue == null ? 0 : runningQueue.size();

			if(m_log.isDebugEnabled()){
				m_log.debug("isRunNow runningQueue : " + runningQueue);
			}
			
			return queueSize < multiplicity;
		} finally {
			_lock.readUnlock();
		}
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
			
			Queue<JobSessionNodeEntityPK> waitingQueue = waitingCache.get(facilityId);
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			
			if (waitingQueue == null) {
				waitingQueue = new LinkedList<JobSessionNodeEntityPK>();
				waitingCache.put(facilityId, waitingQueue);
			}
			
			if (runningQueue == null) {
				runningQueue = new LinkedList<JobSessionNodeEntityPK>();
				runningCache.put(facilityId, runningQueue);
			}
			
			if ((runningQueue == null || ! runningQueue.contains(pk)) && ! waitingQueue.contains(pk)) {
				m_log.debug("toRunning add waitQueue : " + pk);
				waitingQueue.offer(pk);
			}

			storeWaitingCache(waitingCache);
			storeRunningCache(runningCache);

			if(m_log.isDebugEnabled()){
				for(JobSessionNodeEntityPK q : runningQueue){
					m_log.debug("toRunning runningQueue : " + q);
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
			if (runningQueue == null) {
				m_log.warn("fromRunning " + pk);
				return false;
			}
			
			if (runningQueue.remove(pk)) { //// runningQueueから削除
				storeRunningCache(runningCache);
			} else {
				// 普通は実行中から停止に遷移するが、
				// 実行中以外(待機など)から、停止に遷移することがある。
				m_log.info("fromRunning(). from not-running to stop : " + pk);
			}
			
			if(m_log.isDebugEnabled()){
				for(JobSessionNodeEntityPK q : runningQueue){
					m_log.debug("fromRunning runningQueue : " + q);
				}
			}

			// 同一facilityIDのノードで待機中のジョブを実行状態に遷移。
			kick(facilityId);
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
			
			Queue<JobSessionNodeEntityPK> waitingQueue = waitingCache.get(facilityId);
			Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
			
			if (waitingQueue == null || waitingQueue.size() == 0) {
				return;
			}
			
			if (runningQueue == null) {
				runningQueue = new LinkedList<JobSessionNodeEntityPK>();
				runningCache.put(facilityId, runningQueue);
			}
			
			if (isRunNow(facilityId)) {
				JpaTransactionManager jtm = new JpaTransactionManager();
				try {
					jtm.begin();
					JobSessionNodeEntityPK pk = waitingQueue.peek(); //// waitQueueから取得(まだ削除はしない)
					m_log.debug("kick remove waitQueue : " + pk);
					int status = new JobSessionNodeImpl().wait2running(pk);
					// ジョブノードを実行済み
					if (status == 0) {
						m_log.debug("kick add runningQueue : " + pk);
						waitingQueue.poll(); //// waitQueueから削除
						runningQueue.offer(pk); //// runningQueueに追加
						kickFlag = true;
					}
					// ジョブノードを実行しない（ジョブ詳細の実行状態が「実行中」ではない）
					else if (status == 1) {
						m_log.debug("kick not add runningQueue : " + pk);
						waitingQueue.poll(); //// waitQueueから削除
						kickFlag = true;
					}
					jtm.commit();
				} catch (Exception e) {
					m_log.warn("kick : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
				} finally {
					jtm.close();
				}
			}
			
			storeWaitingCache(waitingCache);
			storeRunningCache(runningCache);
			
			if(m_log.isDebugEnabled()){
				for(JobSessionNodeEntityPK q : runningQueue){
					m_log.debug("kick runningQueue : " + q);
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
				storeWaitingCache(new HashMap<String, Queue<JobSessionNodeEntityPK>>());
				
				// runningQueueの再構築
				{
					// オブジェクト権限チェックなし
					List<JobSessionNodeEntity> nodeList = em.createNamedQuery("JobSessionNodeEntity.findByStatus", JobSessionNodeEntity.class, ObjectPrivilegeMode.NONE)
							.setParameter("status", StatusConstant.TYPE_RUNNING).getResultList();
					for (JobSessionNodeEntity node : nodeList) {
						String facilityId = node.getId().getFacilityId();
						Queue<JobSessionNodeEntityPK> runningQueue = runningCache.get(facilityId);
						if (runningQueue == null) {
							runningQueue = new LinkedList<JobSessionNodeEntityPK>();
							runningCache.put(facilityId, runningQueue);
						}
						m_log.debug("refresh add runningQueue : " + node.getId());
						runningQueue.offer(node.getId());
					}
				}
				
				storeRunningCache(runningCache);
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
							&& job.getJobInfoEntity().getJobType() != JobConstant.TYPE_MONITORJOB) {
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
					new JobSessionNodeImpl().startNode(id.getSessionId(), id.getJobunitId(), id.getJobId());
				} catch (InvalidRole e) {
					m_log.warn("refresh " + e.getMessage());
				} catch (JobInfoNotFound e) {
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
	}

	public static String getJobQueueStr() {
		StringBuilder message = new StringBuilder();
		try {
			_lock.readLock();

			Map<String, Queue<JobSessionNodeEntityPK>> runningCache = getRunningCache();
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

}
