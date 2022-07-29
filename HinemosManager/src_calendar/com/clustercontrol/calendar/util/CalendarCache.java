/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.CalendarNotFound;

/**
 * カレンダのキャッシュを保持するクラス
 * オーナーロールに寄らず、全てのカレンダを保持します。
 */
public class CalendarCache {

	private static Log m_log = LogFactory.getLog( CalendarCache.class );
	
	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(CalendarCache.class.getName());
		
		try {
			_lock.writeLock();
			
			Map<String, CalendarInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				init();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<String, CalendarInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_CALENDAR);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_CALENDAR + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<String, CalendarInfo>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<String, CalendarInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_CALENDAR + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_CALENDAR, newCache);
	}
	
	public static void init() {
		try {
			_lock.writeLock();
			
			storeCache(new ConcurrentHashMap<String, CalendarInfo>());
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * キャッシュをリフレッシュする。
	 * カレンダの登録、変更、削除時に呼ぶ。
	 */
	public static void remove(String id) {
		m_log.info("remove() calendar cache is removed");

		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, CalendarInfo> cache = getCache();
			cache.remove(id);
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * id一致するCalendarInfoを返す。
	 * 一致しなければ、キャッシュに追加する
	 * @param id
	 * @return
	 * @throws CalendarNotFound
	 */
	public static CalendarInfo getCalendarInfo(String id) throws CalendarNotFound {
		m_log.debug("getCalendarInfo(" + id + ")");

		if (id == null) {
			return null;
		}
		
		{
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			Map<String, CalendarInfo> cache = getCache();
			CalendarInfo calendar = cache.get(id);
			if (calendar != null) {
				return calendar;
			}
		}
		
		// getCache後からここまでの間に他スレッドによりキャッシュが格納される可能性があり、多重の無駄なキャッシュ格納処理の場合がある。
		// ただし、キャッシュが破損するわけでないため、本方式にて段階的なキャッシングの仕組みを採用する。
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();
			
			ConcurrentHashMap<String, CalendarInfo> cache = getCache();
			CalendarInfo calendar = getCalendarInfoDB(id);
			em.refresh(calendar);
			em.detach(calendar);
			cache.put(id, calendar);
			storeCache(cache);
			
			m_log.trace("CalendarInfo: " + calendar);
			return calendar;
		} finally {
			_lock.writeUnlock();
		}
	}
	/**
	 * IDと一致するカレンダ情報一覧をDBより取得します。
	 * @param id
	 * @return
	 * @throws CalendarNotFound
	 */
	private static CalendarInfo getCalendarInfoDB(String id) throws CalendarNotFound{
		m_log.debug("getCalendarInfoDB(" + id + ")");

		//カレンダ取得
		return QueryUtil.getCalInfoPK_NONE(id);
	}
}
