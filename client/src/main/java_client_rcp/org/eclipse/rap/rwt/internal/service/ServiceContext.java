/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.rwt.internal.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceContext for RCP (Dummy class)
 * 
 * @since 5.0.0
 */
public class ServiceContext{
	private Map<Class<?>, Object> singletonMap = new ConcurrentHashMap<>();

	public Map<Class<?>, Object> getSingletonMap(){
		return singletonMap;
	}

	public void setSingletonMap( Map<Class<?>, Object> singletonMap ){
		this.singletonMap = singletonMap;
	}

}
