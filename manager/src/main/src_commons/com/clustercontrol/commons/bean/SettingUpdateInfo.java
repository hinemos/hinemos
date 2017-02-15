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

package com.clustercontrol.commons.bean;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.util.HinemosTime;

/**
 * Manager外のコンポーネントが利用する設定情報の最終更新日時を保存するクラス
 * @since 4.0
 */
public class SettingUpdateInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log _log = LogFactory.getLog(SettingUpdateInfo.class);
	
	private static SettingUpdateInfo instance = new SettingUpdateInfo();
	
	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(UserRoleCache.class.getName());
		
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			if (cache == null) {	// not null when clustered
				storeCache(new SettingUpdateTimestamp());
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	private static SettingUpdateTimestamp getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_COMMON_SETTING_UPDATE);
		if (_log.isDebugEnabled()) _log.debug("get cache " + AbstractCacheManager.KEY_COMMON_SETTING_UPDATE + " : " + cache);
		return cache == null ? null : (SettingUpdateTimestamp)cache;
	}
	
	private static void storeCache(SettingUpdateTimestamp newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (_log.isDebugEnabled()) _log.debug("store cache " + AbstractCacheManager.KEY_COMMON_SETTING_UPDATE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_COMMON_SETTING_UPDATE, newCache);
	}
	
	private SettingUpdateInfo() {

	}

	public static SettingUpdateInfo getInstance() {
		return instance;
	}

	public void setRepositoryUpdateTime(long time) {
		_log.info("RepositoryUpdateTime is refreshed " + new Date(time));
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.repositoryUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getRepositoryUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.repositoryUpdateTime;
	}

	public void setCalendarUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.calendarUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getCalendarUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.calendarUpdateTime;
	}

	public void setCustomMonitorUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.customMonitorUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getCustomMonitorUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.customMonitorUpdateTime;
	}

	public void setLogFileMonitorUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.logFileMonitorUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getLogFileMonitorUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.logFileMonitorUpdateTime;
	}

	public void setSystemLogMonitorUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.systemLogMonitorUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getSystemLogMonitorUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.systemLogMonitorUpdateTime;
	}

	public void setSnmptrapMonitorUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.snmptrapMonitorUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getSnmptrapMonitorUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.snmptrapMonitorUpdateTime;
	}

	public long getWinEventMonitorUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.winEventMonitorUpdateTime;
	}

	public void setWinEventMonitorUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.winEventMonitorUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getJobFileCheckUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.jobFileCheckUpdateTime;
	}

	public void setJobFileCheckUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.jobFileCheckUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public void setCustomTrapMonitorUpdateTime(long time) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.customtrapMonitorUpdateTime = time;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public long getCustomTrapMonitorUpdateTime() {
		SettingUpdateTimestamp cache = getCache();
		return cache.customtrapMonitorUpdateTime;
	}
	
	
	
	public long getHinemosTimeOffset() {
		SettingUpdateTimestamp cache = getCache();
		return cache.hinemosTimeOffset;
	}

	public void setHinemosTimeOffset(long offset) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.hinemosTimeOffset = offset;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public int getHinemosTimeZoneOffset() {
		SettingUpdateTimestamp cache = getCache();
		return cache.hinemosTimeZoneOffset;
	}

	public void setHinemosTimeZoneOffset(int timeZoneOffset) {
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			cache.hinemosTimeZoneOffset = timeZoneOffset;
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}
	
	private static class SettingUpdateTimestamp implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private long calendarUpdateTime = HinemosTime.currentTimeMillis();
		private long repositoryUpdateTime = HinemosTime.currentTimeMillis();

		private long customMonitorUpdateTime = HinemosTime.currentTimeMillis();
		private long logFileMonitorUpdateTime = HinemosTime.currentTimeMillis();
		private long systemLogMonitorUpdateTime = HinemosTime.currentTimeMillis();
		private long snmptrapMonitorUpdateTime = HinemosTime.currentTimeMillis();
		private long winEventMonitorUpdateTime = HinemosTime.currentTimeMillis();
		private long jobFileCheckUpdateTime = HinemosTime.currentTimeMillis();
		private long customtrapMonitorUpdateTime = HinemosTime.currentTimeMillis();
		private long hinemosTimeOffset = HinemosTime.getTimeOffsetMillis();
		private int hinemosTimeZoneOffset = HinemosTime.getTimeZoneOffset();
		
	}
	
	public static void init() {
		long now = HinemosTime.currentTimeMillis();
		
		try {
			_lock.writeLock();
			
			SettingUpdateTimestamp cache = getCache();
			
			cache.calendarUpdateTime = now;
			cache.repositoryUpdateTime = now;
			cache.customMonitorUpdateTime = now;
			cache.logFileMonitorUpdateTime = now;
			cache.systemLogMonitorUpdateTime = now;
			cache.snmptrapMonitorUpdateTime = now;
			cache.winEventMonitorUpdateTime = now;
			cache.jobFileCheckUpdateTime = now;
			cache.hinemosTimeOffset = HinemosTime.getTimeOffsetMillis();
			cache.hinemosTimeZoneOffset = HinemosTime.getTimeZoneOffset();
			
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
		
	}
	
}
