/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

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
 * OAuth認証時のアクセストークンのキャッシュ
 *
 * @version 6.2.2
 * @since 6.2.2
 */
public class AccessTokenCache {

	private static Log m_log = LogFactory.getLog( AccessTokenCache.class );

	private static final ILock _lock;

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(AccessTokenCache.class.getName());
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue> cache = getCache();
			if (cache == null) {	// not null when clustered
				update(null,  null);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * アクセストークン情報のvalueクラスです。
	 */
	public static class AccessTokenValue {
		private AccessTokenValuePK id;
		private String accessToken;

		public AccessTokenValue(AccessTokenValuePK pk) {
			this.setId(pk);
		}

		public AccessTokenValuePK getId() {
			return this.id;
		}

		public void setId(AccessTokenValuePK id) {
			this.id = id;
		}

		public String getAccessToken() {
			return this.accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
	}

	/**
	 * アクセストークン情報のキークラスです。
	 */
	public static class AccessTokenValuePK {

		private String refreshToken;

		public AccessTokenValuePK(String refreshToken) {
			this.setRefreshToken(refreshToken);
		}

		public void setRefreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof AccessTokenValuePK)) {
				return false;
			}
			AccessTokenValuePK castOther = (AccessTokenValuePK)other;
			return this.refreshToken.equals(castOther.refreshToken);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 17;
			hash = hash * prime + this.refreshToken.hashCode();

			return hash;
		}

		@Override
		public String toString() {
			String[] names = {
					"refreshToken"
			};
			String[] values = {
					this.refreshToken
			};
			return Arrays.toString(names) + " = " + Arrays.toString(values);
		}
	}

	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_ACCESS_TOKEN_VALUE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_ACCESS_TOKEN_VALUE + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_ACCESS_TOKEN_VALUE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_ACCESS_TOKEN_VALUE, newCache);
	}

	/**
	 * キャッシュの更新を行います。
	 * 
	 * @param refreshToken リフレッシュトークン
	 * @param valueEntity アクセストークン結果情報
	 */
	public static void update(String refreshToken, AccessTokenValue valueEntity) {
		try {
			_lock.writeLock();

			ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue> cache 
				= new ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue>();

			AccessTokenValuePK valueEntityPk = new AccessTokenValuePK(refreshToken);

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
			m_log.debug("update cachesize : " + getCache().size());
		}
	}

	/**
	 * キャッシュされているアクセストークン情報を取得します。
	 * 
	 * @param refreshToken リフレッシュトークン
	 * @return アクセストークン情報
	 */
	public static AccessTokenValue getAccessTokenValue(String refreshToken) {

		AccessTokenValuePK valueEntityPk = new AccessTokenValuePK(refreshToken);
		ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue> cache = getCache();
		if (cache.get(valueEntityPk) == null) {
			return new AccessTokenValue(valueEntityPk);
		}
		return cache.get(valueEntityPk);
	}

	/**
	 * キャッシュから特例のアクセストークン情報を削除します。
	 * 
	 * @param refreshToken リフレッシュトークン
	 */
	public static void remove(String refreshToken) {
		try {
			_lock.writeLock();
			AccessTokenValuePK valueEntityPk = new AccessTokenValuePK(refreshToken);
			ConcurrentHashMap<AccessTokenValuePK, AccessTokenValue> cache = getCache();
			cache.remove(valueEntityPk);
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("remove() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
		}
	}
}
