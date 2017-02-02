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

package com.clustercontrol.commons.quartz.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScheduledTaskExecutorFactory {
	
	public static final Log _log = LogFactory.getLog(ScheduledTaskExecutorFactory.class);
	
	private static final ScheduledTaskExecutorFactory _instance = new ScheduledTaskExecutorFactory();
	private static final IScheduledTaskExecutor _taskExecutor;
	
	static {
		String className = null;
		IScheduledTaskExecutor taskExecutor = new LocalScheduledTaskExecutor();
		try {
			className = System.getProperty("hinemos.scheduledtask.executor.class2", LocalScheduledTaskExecutor.class.getName());
			@SuppressWarnings("unchecked")
			Class<? extends IScheduledTaskExecutor> clazz = (Class<? extends IScheduledTaskExecutor>)Class.forName(className);
			
			if (clazz != null) {
				taskExecutor = clazz.newInstance();
			}
		} catch (Exception e) {
			_log.warn("cache manager " + className + " not found.", e);
		} finally {
			_taskExecutor = taskExecutor;
			_log.info("initialized cache manager : " + _taskExecutor.getClass().getName());
		}
	}
	
	private ScheduledTaskExecutorFactory() { }
	
	public static ScheduledTaskExecutorFactory instance() {
		return _instance;
	}
	
	public IScheduledTaskExecutor create() {
		return _taskExecutor;
	}
	
}
