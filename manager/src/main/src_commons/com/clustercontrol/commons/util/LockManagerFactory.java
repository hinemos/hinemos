/*

Copyright (C) 2015 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LockManagerFactory {
	
	public static final Log _log = LogFactory.getLog(LockManagerFactory.class);
	
	private static final LockManagerFactory _instance = new LockManagerFactory();
	private static final ILockManager _lockManager;
	
	static {
		String className = null;
		ILockManager lockManager = new LocalLockManager();
		try {
			className = System.getProperty("hinemos.lockmanager.class", LocalLockManager.class.getName());
			@SuppressWarnings("unchecked")
			Class<? extends ILockManager> clazz = (Class<? extends ILockManager>)Class.forName(className);
			
			if (clazz != null) {
				lockManager = clazz.newInstance();
			}
		} catch (Exception e) {
			_log.warn("lock manager " + className + " not found.", e);
		} finally {
			_lockManager = lockManager;
			_log.info("initialized lock manager : " + _lockManager.getClass().getName());
		}
	}
	
	private LockManagerFactory() { }
	
	public static LockManagerFactory instance() {
		return _instance;
	}
	
	public ILockManager create() {
		return _lockManager;
	}
	
}
