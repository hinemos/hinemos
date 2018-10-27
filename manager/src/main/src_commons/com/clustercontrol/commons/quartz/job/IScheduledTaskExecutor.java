/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.quartz.job;

import java.io.Serializable;

import com.clustercontrol.plugin.util.scheduler.JobExecutionException;

/**
 *
 */
public interface IScheduledTaskExecutor {
	
	void execute(String className, String methodName, Class<? extends Serializable>[] argsType, Serializable[] args) throws JobExecutionException;
	
}
