/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;


/**
 * カレンダパターンのキャッシュを保持するクラス
 * オーナーロールに寄らず、全てのカレンダパターンを保持します。
 */
public class CalendarPatternCache {

	private static Log m_log = LogFactory.getLog( CalendarPatternCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(CalendarPatternCache.class.getName());
		
		try {
			_lock.writeLock();
			
			// initialize cache
			Map<String, CalendarPatternInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				init();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, CalendarPatternInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_CALENDAR_PATTERN);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_CALENDAR_PATTERN + " : " + cache);
		return cache == null ? null : (HashMap<String, CalendarPatternInfo>)cache;
	}
	
	private static void storeCache(HashMap<String, CalendarPatternInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_CALENDAR_PATTERN + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_CALENDAR_PATTERN, newCache);
	}
	
	public static void init() {
		try {
			_lock.writeLock();
			
			storeCache(new HashMap<String, CalendarPatternInfo>());
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * キャッシュをリフレッシュする。
	 * カレンダパターンの登録、変更、削除時に呼ぶ。
	 */
	public static void remove(String id) {
		m_log.info("remove() calendar pattern cache is removed");
		
		try {
			_lock.writeLock();
			
			HashMap<String, CalendarPatternInfo> cache = getCache();
			cache.remove(id);
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * id一致するCalendarPatternInfoを返す。
	 * 一致しなければ、キャッシュに追加する
	 * @param id
	 * @return
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public static CalendarPatternInfo getCalendarPatternInfo(String id) throws CalendarNotFound, InvalidRole {
		m_log.debug("getCalendarPatternInfo(" + id + ")");
		
		try {
			_lock.readLock();
			
			Map<String, CalendarPatternInfo> cache = getCache();
			CalendarPatternInfo pattern = cache.get(id);
			
			if (pattern != null) {
				return pattern;
			}
		} finally {
			_lock.readUnlock();
		}
		
		// 参照に基づ更新がアトミックに行われないため、スレッド間でコンフリクトする可能性がある。
		// ただし、コンフリクトしてもキャッシュが破損するわけでなく、warm up後はマルチスレッドによる高性能が期待できる。
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();
			HashMap<String, CalendarPatternInfo> cache = getCache();
			CalendarPatternInfo pattern = getCalendarPatternInfoDB(id);
			em.refresh(pattern);
			cache.put(id, pattern);
			storeCache(cache);
			
			m_log.trace("CalendarPatternInfo: " + pattern);
			return pattern;
		} finally {
			_lock.writeUnlock();
		}
	}

	private static CalendarPatternInfo getCalendarPatternInfoDB(String id) throws CalendarNotFound, InvalidRole {
		m_log.debug("getCalendarPatternInfoDB(" + id + ")");
		
		//カレンダ取得
		CalendarPatternInfo entity = QueryUtil.getCalPatternInfoPK(id, ObjectPrivilegeMode.NONE);
		
		return entity;
	}
}
