/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.factory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * 
 * 共通設定情報をキャッシュするクラスです。
 * 
 * @since 5.0.0
 * @version 5.0.0
 * 
 */
public class HinemosPropertyInfoCache {
	private static Log log = LogFactory.getLog(HinemosPropertyInfoCache.class);
	
	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(HinemosPropertyInfoCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<String, HinemosPropertyInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, HinemosPropertyInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_COMMON_PROPERTY);
		if (log.isDebugEnabled()) log.debug("get cache " + AbstractCacheManager.KEY_COMMON_PROPERTY + " : " + cache);
		return cache == null ? null : (HashMap<String, HinemosPropertyInfo>)cache;
	}
	
	private static void storeCache(HashMap<String, HinemosPropertyInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (log.isDebugEnabled()) log.debug("store cache " + AbstractCacheManager.KEY_COMMON_PROPERTY + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_COMMON_PROPERTY, newCache);
	}

	private HinemosPropertyInfoCache() {
	}

	/**
	 * プロパティを取得
	 * 
	 * @param key プロパティのキー
	 * @return プロパティ
	 */
	public static HinemosPropertyInfo getProperty(String key) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, HinemosPropertyInfo> cache = getCache();
		return cache == null ? null : cache.get(key);
	}

	/**
	 * キャッシュを更新
	 */
	public static synchronized void refresh() {
		try {
			_lock.writeLock();
			
			long startTime = HinemosTime.currentTimeMillis();
			
			HashMap<String, HinemosPropertyInfo> cache = createHinemosPropertyInfoMap();
			storeCache(cache);
			
			log.info(String.format("refresh: %dms", HinemosTime.currentTimeMillis() - startTime));
		} finally {
			_lock.writeUnlock();
		}
	}

	private static HashMap<String, HinemosPropertyInfo> createHinemosPropertyInfoMap() {
		HashMap<String, HinemosPropertyInfo> infoMap = new HashMap<String, HinemosPropertyInfo>();
		List<HinemosPropertyInfo> entities = QueryUtil.getAllHinemosProperty_None();
		for (HinemosPropertyInfo entity : entities) {
			infoMap.put(entity.getKey(), entity);
		}
		return infoMap;
	}
}
