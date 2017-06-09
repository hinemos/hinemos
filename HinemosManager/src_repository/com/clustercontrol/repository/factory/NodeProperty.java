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

package com.clustercontrol.repository.factory;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * ノード用プロパティを作成するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeProperty {

	private static Log m_log = LogFactory.getLog(NodeProperty.class);

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(NodeProperty.class.getName());
		
		try {
			_lock.writeLock();
			
			Map<String, NodeInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				init();
			}
		} catch (Throwable t) {
			m_log.error("NodeProperty initialisation error. " + t.getMessage(), t);
		} finally {
			_lock.writeUnlock();
		}
	}
	
	/** ----- 初期値キャッシュ ----- */
	
	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<String, NodeInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_NODE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_NODE + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<String, NodeInfo>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<String, NodeInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_NODE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_NODE, newCache);
	}

	public static void removeNode (String facilityId) {
		m_log.info("remove NodeCache : " + facilityId);
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			cache.remove(facilityId);
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public static void updateNode (String facilityId) {
		m_log.info("update NodeCache : " + facilityId);
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			try {
				new JpaTransactionManager().getEntityManager().clear();
				NodeInfo facilityEntity = QueryUtil.getNodePK(facilityId);
				cache.put(facilityId, facilityEntity);
			} catch (Exception e) {
				m_log.warn("update NodeCache failed : " + e.getMessage());
				//例外発生時は古い値がキャッシュに残らないように削除する
				cache.remove(facilityId);
			}
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public static void init() {
		JpaTransactionManager jtm = null;
		try {
			_lock.writeLock();
			
			long startTime = System.currentTimeMillis();
			jtm = new JpaTransactionManager();
			jtm.getEntityManager().clear();
			
			ConcurrentHashMap<String, NodeInfo> cache = new ConcurrentHashMap<String, NodeInfo>();
			
			for (NodeInfo node : QueryUtil.getAllNode_NONE()) {
				cache.put(node.getFacilityId(), node);
			}
			
			storeCache(cache);
			
			m_log.info("init cache " + (System.currentTimeMillis() - startTime) + "ms. size=" + cache.size());
		} finally {
			if(jtm != null) {
				jtm.close();
			}
			_lock.writeUnlock();
		}
	}

	/**
	 * 与えられたファシリティIDに基づき、該当するノード情報を返す。<BR>
	 *
	 * @param facilityId ファシリティID
	 * @param mode ノード情報扱い種別（参照、追加、変更）
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getProperty(String facilityId) throws FacilityNotFound {
		m_log.debug("getProperty() : facilityId = " + facilityId);

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		}

		{
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			
			NodeInfo nodeInfo = cache.get(facilityId);
			if (nodeInfo != null) {
				if (!facilityId.equals(nodeInfo.getFacilityId())) {
					// 試験中に怪しい挙動があったので、一応ログを仕込んでおく。
					m_log.error("cache is broken." + facilityId + "," + nodeInfo.getFacilityId());
				}
				return nodeInfo;
			}
		}
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			
			NodeInfo facilityEntity = QueryUtil.getNodePK(facilityId);
			cache.put(facilityId, facilityEntity);
			storeCache(cache);
			
			return facilityEntity;
		} finally {
			_lock.writeUnlock();
		}
	}
}
