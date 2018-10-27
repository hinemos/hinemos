/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.factory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;

/**
 * カスタム監視のキャッシュ
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class MonitorCustomCache {
	private static Log m_log = LogFactory.getLog( MonitorCustomCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorCustomCache.class.getName());
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<MonitorCustomValuePK, MonitorCustomValue> cache = getCache();
			if (cache == null) {	// not null when clustered
				update(null, null, null, null);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * カスタム監視結果情報
	 */
	public static class MonitorCustomValue {
		private MonitorCustomValuePK id;
		private Long getDate;
		private Object value;

		public MonitorCustomValue(MonitorCustomValuePK pk) {
			this.setId(pk);
		}

		public MonitorCustomValuePK getId() {
			return this.id;
		}

		public void setId(MonitorCustomValuePK id) {
			this.id = id;
		}

		public Long getGetDate() {
			return this.getDate;
		}

		public void setGetDate(Long getDate) {
			this.getDate = getDate;
		}

		public Object getValue() {
			return this.value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

	}

	/**
	 * カスタム監視結果キー情報
	 */
	public static class MonitorCustomValuePK {

		private String monitorId;
		private String facilityId;
		private String key;

		public MonitorCustomValuePK(String monitorId, String facilityId, String key) {
			this.setMonitorId(monitorId);
			this.setFacilityId(facilityId);
			this.setKey(key);
		}

		public void setMonitorId(String monitorId) {
			this.monitorId = monitorId;
		}

		public void setFacilityId(String facilityId) {
			this.facilityId = facilityId;
		}
		
		public void setKey(String key){
			this.key = key;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof MonitorCustomValuePK)) {
				return false;
			}
			MonitorCustomValuePK castOther = (MonitorCustomValuePK)other;
			return
					this.monitorId.equals(castOther.monitorId)
					&& this.facilityId.equals(castOther.facilityId)
					&& this.key.equals(castOther.key);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 17;
			hash = hash * prime + this.monitorId.hashCode();
			hash = hash * prime + this.facilityId.hashCode();
			hash = hash * prime + this.key.hashCode();

			return hash;
		}

		@Override
		public String toString() {
			String[] names = {
					"monitorId",
					"facilityId",
					"key"
			};
			String[] values = {
					this.monitorId,
					this.facilityId,
					this.key
			};
			return Arrays.toString(names) + " = " + Arrays.toString(values);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<MonitorCustomValuePK, MonitorCustomValue> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_CUSTOM_VALUE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_CUSTOM_VALUE + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<MonitorCustomValuePK, MonitorCustomValue>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<MonitorCustomValuePK, MonitorCustomValue> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_CUSTOM_VALUE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_CUSTOM_VALUE, newCache);
	}

	/**
	 * キャッシュの更新を行う。
	 * @param m_monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param valueEntity カスタム監視結果情報
	 */
	public static void update(String m_monitorId, String facilityId, String key, MonitorCustomValue valueEntity) {
		try {
			_lock.writeLock();

			ConcurrentHashMap<MonitorCustomValuePK, MonitorCustomValue> cache 
				= new ConcurrentHashMap<MonitorCustomValuePK, MonitorCustomValue>();

			MonitorCustomValuePK valueEntityPk = new MonitorCustomValuePK(m_monitorId, facilityId, key);

			if (valueEntityPk != null && getCache() != null) {
				cache.putAll(getCache());
				if (cache.containsKey(valueEntityPk)) {
					cache.remove(valueEntityPk);
				}
				cache.put(valueEntityPk, valueEntity);
			}
			storeCache(cache);

		} catch (Exception e) {
			m_log.warn("update() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("MonitorCustomCache update cachesize : " + getCache().size());
		}
	}

	/**
	 * キャッシュされているカスタム監視結果を取得する。
	 * @param m_monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @return カスタム監視結果情報
	 */
	public static MonitorCustomValue getMonitorCustomValue(String m_monitorId, String facilityId, String key) {

		MonitorCustomValuePK valueEntityPk = new MonitorCustomValuePK(m_monitorId, facilityId, key);
		ConcurrentHashMap<MonitorCustomValuePK, MonitorCustomValue> cache = getCache();
		if (cache.get(valueEntityPk) == null) {
			return new MonitorCustomValue(valueEntityPk);
		}
		return cache.get(valueEntityPk);
	}

}
