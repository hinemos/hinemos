/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheManagerFactory {
	
	public static final Log _log = LogFactory.getLog(CacheManagerFactory.class);
	
	private static final CacheManagerFactory _instance = new CacheManagerFactory();
	private static final AbstractCacheManager _cacheManager;
	
	static {
		String className = null;
		AbstractCacheManager cacheManager = new LocalCacheManager();
		try {
			className = System.getProperty("hinemos.cachemanager.class", LocalCacheManager.class.getName());
			@SuppressWarnings("unchecked")
			Class<? extends AbstractCacheManager> clazz = (Class<? extends AbstractCacheManager>)Class.forName(className);
			
			if (clazz != null) {
				cacheManager = clazz.newInstance();
			}
		} catch (Exception e) {
			_log.warn("cache manager " + className + " not found.", e);
		} finally {
			_cacheManager = cacheManager;
			_log.info("initialized cache manager : " + _cacheManager.getClass().getName());
		}
	}
	
	private CacheManagerFactory() { }
	
	public static CacheManagerFactory instance() {
		return _instance;
	}
	
	public AbstractCacheManager create() {
		return _cacheManager;
	}
	
}
