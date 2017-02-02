/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

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
