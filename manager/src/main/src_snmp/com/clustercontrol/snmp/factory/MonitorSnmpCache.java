package com.clustercontrol.snmp.factory;

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
 * SNMP監視のキャッシュ
 *
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class MonitorSnmpCache {
	private static Log m_log = LogFactory.getLog(MonitorSnmpCache.class );

	private static Object cacheLock = new Object();
	private static final ILock _lock;

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorSnmpCache.class.getName());
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<MonitorSnmpValuePK, MonitorSnmpValue> cache = getCache();
			if (cache == null) {	// not null when clustered
				update(null, null, null);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * SNMP監視結果情報
	 */
	public static class MonitorSnmpValue {
		private MonitorSnmpValuePK id;
		private Long getDate;
		private Double value;

		public MonitorSnmpValue(MonitorSnmpValuePK pk) {
			this.setId(pk);
		}

		public MonitorSnmpValuePK getId() {
			return this.id;
		}

		public void setId(MonitorSnmpValuePK id) {
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
	 * SNMP監視結果キー情報
	 */
	public static class MonitorSnmpValuePK {

		private String monitorId;
		private String facilityId;

		public MonitorSnmpValuePK(String monitorId, String facilityId) {
			this.setMonitorId(monitorId);
			this.setFacilityId(facilityId);
		}

		public void setMonitorId(String monitorId) {
			this.monitorId = monitorId;
		}

		public void setFacilityId(String facilityId) {
			this.facilityId = facilityId;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof MonitorSnmpValuePK)) {
				return false;
			}
			MonitorSnmpValuePK castOther = (MonitorSnmpValuePK)other;
			return
					this.monitorId.equals(castOther.monitorId)
					&& this.facilityId.equals(castOther.facilityId);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 17;
			hash = hash * prime + this.monitorId.hashCode();
			hash = hash * prime + this.facilityId.hashCode();

			return hash;
		}

		@Override
		public String toString() {
			String[] names = {
					"monitorId",
					"facilityId"
			};
			String[] values = {
					this.monitorId,
					this.facilityId
			};
			return Arrays.toString(names) + " = " + Arrays.toString(values);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<MonitorSnmpValuePK, MonitorSnmpValue> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_SNMP_VALUE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_SNMP_VALUE + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<MonitorSnmpValuePK, MonitorSnmpValue>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<MonitorSnmpValuePK, MonitorSnmpValue> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_SNMP_VALUE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_SNMP_VALUE, newCache);
	}

	/**
	 * キャッシュの更新を行う。
	 * @param m_monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param valueEntity SNMP監視結果情報
	 */
	public static void update(String m_monitorId, String facilityId, MonitorSnmpValue valueEntity) {
		try {
			_lock.writeLock();

			ConcurrentHashMap<MonitorSnmpValuePK, MonitorSnmpValue> cache 
				= new ConcurrentHashMap<MonitorSnmpValuePK, MonitorSnmpValue>();

			MonitorSnmpValuePK valueEntityPk = new MonitorSnmpValuePK(m_monitorId, facilityId);

			synchronized (cacheLock) {
				if (valueEntityPk != null && getCache() != null) {
					cache.putAll(getCache());
					if (cache.containsKey(valueEntityPk)) cache.remove(valueEntityPk);
					cache.put(valueEntityPk, valueEntity);
				}
				storeCache(cache);
			}

		} catch (Exception e) {
			m_log.warn("update() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("MonitorSnmpCache update cachesize : " + getCache().size());
		}
	}

	/**
	 * キャッシュされているSNMP監視結果を取得する。
	 * @param pk キー情報
	 * @return SNMP監視結果情報
	 */
	public static MonitorSnmpValue getMonitorSnmpValue(String m_monitorId, String facilityId) {

		MonitorSnmpValuePK valueEntityPk = new MonitorSnmpValuePK(m_monitorId, facilityId);
		ConcurrentHashMap<MonitorSnmpValuePK, MonitorSnmpValue> cache = getCache();
		if (cache.get(valueEntityPk) == null) {
			return new MonitorSnmpValue(valueEntityPk);
		}
		return cache.get(valueEntityPk);
	}
}
