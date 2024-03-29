/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.quartz.job;

import java.io.Serializable;

import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.plugin.util.scheduler.Job;
import com.clustercontrol.plugin.util.scheduler.JobDataMap;
import com.clustercontrol.plugin.util.scheduler.JobDetail;
import com.clustercontrol.plugin.util.scheduler.JobExecutionException;

public class ReflectionInvokerJob implements Job {

	public static final String KEY_CLASS_NAME = "CLASS_NAME";
	public static final String KEY_METHOD_NAME = "METHOD_NAME";
	public static final String KEY_ARGS_TYPE = "ARGS_TYPE";
	public static final String KEY_ARGS = "ARGS";
	public static final String KEY_RESET_ON_RESTART = "RESET_ON_RESTART";

	@Override
	public void execute(JobDetail jd) throws JobExecutionException {
		JobDataMap dmap = jd.getJobDataMap();

		String className = dmap.getString(KEY_CLASS_NAME);
		String methodName = dmap.getString(KEY_METHOD_NAME);
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] argsType = (Class<? extends Serializable>[])dmap.get(KEY_ARGS_TYPE);
		Serializable[] args = (Serializable[])dmap.get(KEY_ARGS);
		if (className.equals(JobControllerBean.class.getName())) {
			if (methodName.equals(com.clustercontrol.jobmanagement.bean.QuartzConstant.METHOD_NAME)) {
				args[com.clustercontrol.jobmanagement.bean.QuartzConstant.INDEX_EXECUTE_TIME] = jd.getExecuteTime();
			} else if (methodName.equals(com.clustercontrol.jobmanagement.bean.QuartzConstant.METHOD_NAME_FOR_JOBPREMAKE)
					|| methodName.equals(com.clustercontrol.jobmanagement.bean.QuartzConstant.METHOD_NAME_FOR_JOBPREMAKE_ONCE)) {
				args[com.clustercontrol.jobmanagement.bean.QuartzConstant.INDEX_EXECUTE_TIME_FOR_JOBPREMAKE] = jd.getExecuteTime();
			} else if (methodName.equals(com.clustercontrol.jobmanagement.bean.QuartzConstant.METHOD_NAME_FOR_JOBLINKRCV)) {
				args[com.clustercontrol.jobmanagement.bean.QuartzConstant.INDEX_EXECUTE_TIME_FOR_JOBLINKRCV] = jd.getExecuteTime();
				args[com.clustercontrol.jobmanagement.bean.QuartzConstant.INDEX_PREVIOUS_FIRE_TIME_FOR_JOBLINKRCV] = jd.getPreviousFireTime();
			}
		}

		MonitoredThreadPoolExecutor.beginTask(Thread.currentThread(), className);

		try {
			// ThreadLocalの初期化
			HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);

			IScheduledTaskExecutor exec = ScheduledTaskExecutorFactory.instance().create();
			exec.execute(className, methodName, argsType, args);
		} finally {
			MonitoredThreadPoolExecutor.finishTask(Thread.currentThread());
		}
	}

}
