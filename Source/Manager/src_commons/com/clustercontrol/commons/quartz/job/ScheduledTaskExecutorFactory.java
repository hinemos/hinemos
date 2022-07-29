/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
