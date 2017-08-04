/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.notify.entity.MonitorStatusPK;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.MonitorStatusEntityPK;
import com.clustercontrol.util.HinemosTime;

/**
 * MonitorStatusEntityをキャッシュするクラス
 */
public class MonitorStatusCache {
	private static final Log log = LogFactory.getLog(MonitorStatusCache.class);
	
	static {
		Set<MonitorStatusEntityPK> keySet = cacheKeys();
		
		if (keySet == null || keySet.size() == 0) {
			init();
		}
	}
	
	private static ILock getLock(MonitorStatusEntityPK pk) {
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(String.format("%s [%s, %s, %s, %s]", MonitorStatusCache.class.getName(), 
				pk.getFacilityId(), pk.getPluginId(), pk.getMonitorId(), pk.getSubKey()));
		return lock;
	}
	
	private static MonitorStatusEntity getCache(MonitorStatusEntityPK pk) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(pk);
		if (log.isDebugEnabled()) log.debug("get cache " + AbstractCacheManager.KEY_NOTIFY_MONITOR_STATUS + " : " + cache);
		return cache == null ? null : (MonitorStatusEntity)cache;
	}
	
	private static void storeCache(MonitorStatusEntityPK pk, MonitorStatusEntity newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (log.isDebugEnabled()) log.debug("store cache " + AbstractCacheManager.KEY_NOTIFY_MONITOR_STATUS + " : " + newCache);
		cm.store(pk, newCache);
	}
	
	private static void removeCache(MonitorStatusEntityPK pk) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (log.isDebugEnabled()) log.debug("remove cache " + AbstractCacheManager.KEY_NOTIFY_MONITOR_STATUS + " : " + pk);
		cm.remove(pk);
	}
	
	private static Set<MonitorStatusEntityPK> cacheKeys() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Set<MonitorStatusEntityPK> cacheKeys = cm.getKeySet(MonitorStatusEntityPK.class);
		return cacheKeys != null ? Collections.unmodifiableSet(cacheKeys): null;
	}

	public static void init() {
		new JpaTransactionManager().getEntityManager().clear();
		List<MonitorStatusEntity> entities = QueryUtil.getAllMonitorStatus();
		long start = System.currentTimeMillis();
		for (MonitorStatusEntity entity : entities) {
			ILock lock = getLock(entity.getId());
			try {
				lock.writeLock();
				
				storeCache(entity.getId(), entity);
			} finally {
				lock.writeUnlock();
			}
		}
		log.info("init MonitorStatusCache " + (System.currentTimeMillis() - start) + "ms. size=" + cacheKeys().size());
	}

	public static void add(MonitorStatusEntity entity) {
		ILock lock = getLock(entity.getId());
		
		try {
			lock.writeLock();
			
			storeCache(entity.getId(), entity);
		} finally {
			lock.writeUnlock();
		}
	}

	public static MonitorStatusEntity get(MonitorStatusEntityPK pk) {
		ILock lock = getLock(pk);
		
		try {
			lock.readLock();
			
			return getCache(pk);
		} finally {
			lock.readUnlock();
		}
	}

	public static MonitorStatusEntity get(MonitorStatusPK pk) {
		MonitorStatusEntityPK key = new MonitorStatusEntityPK(pk.getFacilityId(), pk.getPluginId(), pk.getMonitorId(), pk.getSubKey());
		ILock lock = getLock(key);
		
		try {
			lock.readLock();
			
			return getCache(key);
		} finally {
			lock.readUnlock();
		}
	}

	public static List<MonitorStatusEntity> getByPluginIdAndMonitorId(
			String pluginId, String monitorId) {
		List<MonitorStatusEntity> resultList = new ArrayList<MonitorStatusEntity>();
		
		Set<MonitorStatusEntityPK> keySet = cacheKeys();
		for (MonitorStatusEntityPK key : keySet) {
			if (key.getPluginId().equals(pluginId) && key.getMonitorId().equals(monitorId)) {
				ILock lock = getLock(key);
				
				try {
					lock.readLock();
					
					MonitorStatusEntity cache = getCache(key);
					if (cache != null) {
						resultList.add(cache);
					}
				} finally {
					lock.readUnlock();
				}
			}
		}
		
		return resultList;
	}
	
	public static List<MonitorStatusEntity> getByPluginIdAndMonitorMap(
			String pluginId, Map<String, String> monitorMap) {
		List<MonitorStatusEntity> resultList = new ArrayList<>();
		
		Set<MonitorStatusEntityPK> keySet = cacheKeys();
		for (MonitorStatusEntityPK key : keySet) {
			if (key.getPluginId().equals(pluginId) && monitorMap.containsKey(key.getMonitorId())) {
				ILock lock = getLock(key);
				
				try {
					lock.readLock();
					
					MonitorStatusEntity cache = getCache(key);
					if (cache != null) {
						resultList.add(cache);
					}
				} finally {
					lock.readUnlock();
				}
			}
		}
		
		return resultList;
	}

	public static void remove(MonitorStatusEntityPK pk) {
		ILock lock = getLock(pk);
		
		try {
			lock.writeLock();
			
			removeCache(pk);
		} finally {
			lock.writeUnlock();
		}
	}

	public static void remove(MonitorStatusEntity entity) {
		ILock lock = getLock(entity.getId());
		
		try {
			lock.writeLock();
			
			removeCache(entity.getId());
		} finally {
			lock.writeUnlock();
		}
	}

	public static void update(MonitorStatusEntity entity) {
		ILock lock = getLock(entity.getId());
		
		if (log.isDebugEnabled()) {
			log.debug("update : " + entity);
		}
		
		try {
			lock.writeLock();
			
			storeCache(entity.getId(), entity);
		} finally {
			lock.writeUnlock();
		}
	}

	// persist が多重で動作しないように修正
	private static boolean persistSkip = false;
	
	@SuppressWarnings("deprecation")
	public static void persist() {
		if (persistSkip) {
			log.warn("persist skip");
			return;
		}
		try {
			persistSkip = true;
			long start = HinemosTime.currentTimeMillis();
			List<MonitorStatusEntity> entityList = new ArrayList<MonitorStatusEntity>();
			Set<MonitorStatusEntityPK> keySet = cacheKeys();
			for (MonitorStatusEntityPK key : keySet) {
				ILock lock = getLock(key);
				try {
					lock.readLock();
					
					MonitorStatusEntity cache = getCache(key);
					if (cache != null) {
						entityList.add(cache);
					}
				} finally {
					lock.readUnlock();
				}
			}
			JpaTransactionManager jtm = new JpaTransactionManager();
			try {
				HinemosEntityManager em = jtm.getEntityManager();
				jtm.begin();
	
				//削除
				em.createQuery("delete from MonitorStatusEntity").executeUpdate();
				
				em.flush();
	
				//作成
				for (MonitorStatusEntity entity : entityList) {
					/* MonitorStatusEntityを直接persistすると、eclipselinkのクラスにおいて、MonitorStatusEntity同士の参照が生まれ、
					 * GCで回収されないため、deep copyをpersistする
					 */
					em.persist(entity.clone());
				}
				jtm.commit();
			} catch (Exception e) {
				log.error(e);
				jtm.rollback();
			} finally {
				jtm.close();
			}
			log.info(String.format("persist: %dms, size=%d", HinemosTime.currentTimeMillis() - start, entityList.size()));
		} finally {
			persistSkip = false;
		}
	}
}