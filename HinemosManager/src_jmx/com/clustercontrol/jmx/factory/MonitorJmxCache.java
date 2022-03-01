/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.factory;

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
 * JMX監視のキャッシュ
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class MonitorJmxCache {

	private static Log m_log = LogFactory.getLog( MonitorJmxCache.class );

	private static final ILock _lock;

	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorJmxCache.class.getName());
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<MonitorJmxValuePK, MonitorJmxValue> cache = getCache();
			if (cache == null) {	// not null when clustered
				update(null, null, null, null);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * JMX監視結果情報
	 */
	public static class MonitorJmxValue {
		private MonitorJmxValuePK id;
		private Long getDate;
		private Double value;

		public MonitorJmxValue(MonitorJmxValuePK pk) {
			this.setId(pk);
		}

		public MonitorJmxValuePK getId() {
			return this.id;
		}

		public void setId(MonitorJmxValuePK id) {
			this.id = id;
		}

		public Long getGetDate() {
			return this.getDate;
		}

		public void setGetDate(Long getDate) {
			this.getDate = getDate;
		}

		public Double getValue() {
			return this.value;
		}

		public void setValue(Double value) {
			this.value = value;
		}

	}

	/**
	 * JMX監視結果キー情報
	 */
	public static class MonitorJmxValuePK {

		private String monitorId;
		private String facilityId;
		private String displayName;

		public MonitorJmxValuePK(String monitorId, String facilityId, String displayName) {
			this.setMonitorId(monitorId);
			this.setFacilityId(facilityId);
			this.setDisplayName(displayName);
		}

		public void setMonitorId(String monitorId) {
			this.monitorId = monitorId;
		}

		public void setFacilityId(String facilityId) {
			this.facilityId = facilityId;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof MonitorJmxValuePK)) {
				return false;
			}
			MonitorJmxValuePK castOther = (MonitorJmxValuePK)other;
			return
					this.monitorId.equals(castOther.monitorId)
					&& this.facilityId.equals(castOther.facilityId)
					&& this.displayName.equals(castOther.displayName);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 17;
			hash = hash * prime + this.monitorId.hashCode();
			hash = hash * prime + this.facilityId.hashCode();
			hash = hash * prime + this.displayName.hashCode();

			return hash;
		}

		@Override
		public String toString() {
			String[] names = {
					"monitorId",
					"facilityId",
					"displayName"
			};
			String[] values = {
					this.monitorId,
					this.facilityId,
					this.displayName
			};
			return Arrays.toString(names) + " = " + Arrays.toString(values);
		}
	}

	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<MonitorJmxValuePK, MonitorJmxValue> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JMX_VALUE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JMX_VALUE + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<MonitorJmxValuePK, MonitorJmxValue>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<MonitorJmxValuePK, MonitorJmxValue> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JMX_VALUE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JMX_VALUE, newCache);
	}

	/**
	 * キャッシュの更新を行う。
	 * @param m_monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param displayName 表示名称
	 * @param valueEntity JMX監視結果情報
	 */
	public static void update(String m_monitorId, String facilityId, String displayName, MonitorJmxValue valueEntity) {
		try {
			_lock.writeLock();

			ConcurrentHashMap<MonitorJmxValuePK, MonitorJmxValue> cache 
				= new ConcurrentHashMap<MonitorJmxValuePK, MonitorJmxValue>();

			MonitorJmxValuePK valueEntityPk = new MonitorJmxValuePK(m_monitorId, facilityId, displayName);

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
			m_log.debug("MonitorJmxCache update cachesize : " + getCache().size());
		}
	}

	/**
	 * キャッシュされているJmx監視結果を取得する。
	 * @param m_monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param displayName 表示名称
	 * @return JMX監視結果情報
	 */
	public static MonitorJmxValue getMonitorJmxValue(String m_monitorId, String facilityId, String displayName) {

		MonitorJmxValuePK valueEntityPk = new MonitorJmxValuePK(m_monitorId, facilityId, displayName);
		ConcurrentHashMap<MonitorJmxValuePK, MonitorJmxValue> cache = getCache();
		if (cache.get(valueEntityPk) == null) {
			return new MonitorJmxValue(valueEntityPk);
		}
		return cache.get(valueEntityPk);
	}

}
