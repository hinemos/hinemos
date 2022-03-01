/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

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
	private static final SortedMap<String, Runnable> refreshedEventListeners;

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(HinemosPropertyInfoCache.class.getName());
		refreshedEventListeners = new ConcurrentSkipListMap<>();
		
		try {
			_lock.writeLock();
			HashMap<String, HinemosPropertyInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				refresh();
				cache = getCache();
				if (cache != null) {
					log.info("HinemosProperty cache : " + cache.toString());
				}
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, HinemosPropertyInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_COMMON_PROPERTY);
		return cache == null ? null : (HashMap<String, HinemosPropertyInfo>)cache;
	}
	
	private static void storeCache(HashMap<String, HinemosPropertyInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
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
		try {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, HinemosPropertyInfo> cache = getCache();
		if (log.isDebugEnabled()) {
			if (cache == null) {
				log.debug("getProperty key=" + key + " cache is null ");
			} else {
				log.debug("getProperty key=" + key + " HinemosPropertyInfo[ "  + cache.get(key) + " ]");
			}
			
		}
		return cache == null ? null : cache.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * キャッシュを更新
	 */
	public static synchronized void refresh() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();
			
			long startTime = HinemosTime.currentTimeMillis();
			
			em.clear();
			HashMap<String, HinemosPropertyInfo> cache = createHinemosPropertyInfoMap();
			storeCache(cache);
			
			log.info(String.format("refresh: %dms size=%d", HinemosTime.currentTimeMillis() - startTime, cache.size()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			_lock.writeUnlock();
		}

		// キャッシュ更新後の処理を実行
		for (Entry<String, Runnable> entry : refreshedEventListeners.entrySet()) {
			log.info("refresh: Call " + entry.getKey());
			try {
				entry.getValue().run();
			} catch (Exception e) {
				log.warn("refresh: RefreshedEventHandling failed. ", e);
			}
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
	
	public static void logCache() {
		HashMap<String, HinemosPropertyInfo> cache = getCache();
		if (cache != null) {
			for (HinemosPropertyInfo info : cache.values()) {
				log.info("HinemosPropery Cache:" + info.toString());
			}
		}
	}

	/**
	 * Hinemosプロパティキャッシュの更新時(＝Hinemosプロパティに変更があったとき)に実行する処理を追加します。
	 * 追加した処理は、同期メソッドである {@link #refresh()} 内で、name 昇順に直列的に実行されます。
	 */
	public static void addRefreshedEventListener(String name, Runnable action) {
		refreshedEventListeners.put(name, action);
		log.info("addRefreshedEventListener: " + name);
	}

}
