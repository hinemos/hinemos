/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.quartz.job;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.plugin.util.scheduler.JobExecutionException;

/**
 *
 */
public class LocalScheduledTaskExecutor implements IScheduledTaskExecutor {
	
	private static final Log log = LogFactory.getLog(LocalScheduledTaskExecutor.class);
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.commons.quartz.job.IJobExecutor#execute(java.lang.String, java.lang.String, java.lang.Class[], java.io.Serializable[])
	 */
	@Override
	public void execute(String className, String methodName,
			Class<? extends Serializable>[] argsType, Serializable[] args) throws JobExecutionException {
		try {
			Class<?> clazz = Class.forName(className);
			Object obj = clazz.newInstance();

			if (argsType.length == 0 || args.length == 0) {
				Method method = clazz.getMethod(methodName);
				method.invoke(obj);
			} else {
				Method method = clazz.getMethod(methodName, argsType);
				method.invoke(obj, (Object[])args);
			}
		} catch (Exception e) {
			log.warn("invocation failure. (class = " + className + ", methodName = " + methodName + ")", e);
			throw new JobExecutionException();
		}
	}

}
