/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/*
 * JPA用のConfig
 */
public class JpaPersistenceConfig {

	/** JPAのプロパティ名 */
	public static final String JPA_PARAM_QUERY_TIMEOUT = "jakarta.persistence.query.timeout";

	/** 重複チェック（EntityExistsExceptionチェック）で使用するヒント */
	public static final Map<String, Object> JPA_EXISTS_CHECK_HINT_MAP = new ConcurrentHashMap<String, Object>();

	/** EntityManagerFactory */
	private static EntityManagerFactory hinemosEMFactory = null;

	static {
		JPA_EXISTS_CHECK_HINT_MAP.put("jakarta.persistence.cache.storeMode","REFRESH");
	}
	
	private static Object lock = new Object();
	
	/*
	 * Hinemos用のEntityManagerFactoryを返す
	 */
	public static EntityManagerFactory getHinemosEMFactory() {
		synchronized (lock) {
			if (hinemosEMFactory == null || !hinemosEMFactory.isOpen()) {
				hinemosEMFactory = Persistence.createEntityManagerFactory("hinemos");
			}
		}
		return hinemosEMFactory;
	}
	
	public static String getHinemosJdbcUrl() {
		EntityManagerFactory emf = getHinemosEMFactory();
		return (String)emf.getProperties().get(PersistenceUnitProperties.JDBC_URL);
	}
	
}
