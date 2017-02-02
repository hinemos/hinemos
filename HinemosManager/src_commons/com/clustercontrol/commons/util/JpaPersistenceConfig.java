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

package com.clustercontrol.commons.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

/*
 * JPA用のConfig
 */
public class JpaPersistenceConfig {

	/** JPAのプロパティ名 */
	public static final String JPA_PARAM_QUERY_TIMEOUT = "javax.persistence.query.timeout";

	/** 重複チェック（EntityExistsExceptionチェック）で使用するヒント */
	public static final Map<String, Object> JPA_EXISTS_CHECK_HINT_MAP = new ConcurrentHashMap<String, Object>();

	/** EntityManagerFactory */
	private static EntityManagerFactory hinemosEMFactory = null;

	static {
		JPA_EXISTS_CHECK_HINT_MAP.put("javax.persistence.cache.storeMode","REFRESH");
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
