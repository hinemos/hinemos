/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.accesscontrol.util.OptionManager;
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.commons.bean.ThreadInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.SchedulerInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.selfcheck.monitor.ActivationKeyMonitor;
import com.clustercontrol.selfcheck.monitor.AsyncTaskQueueMonitor;
import com.clustercontrol.selfcheck.monitor.DBConnectionCountMonitor;
import com.clustercontrol.selfcheck.monitor.DBLongTranMonitor;
import com.clustercontrol.selfcheck.monitor.DatabaseMonitor;
import com.clustercontrol.selfcheck.monitor.FileSystemMonitor;
import com.clustercontrol.selfcheck.monitor.JVMHeapMonitor;
import com.clustercontrol.selfcheck.monitor.JobQueueMonitor;
import com.clustercontrol.selfcheck.monitor.JobRunSessionMonitor;
import com.clustercontrol.selfcheck.monitor.RAMSwapOutMonitor;
import com.clustercontrol.selfcheck.monitor.RestServiceQueueMonitor;
import com.clustercontrol.selfcheck.monitor.SchedulerMonitor;
import com.clustercontrol.selfcheck.monitor.SelfCheckMonitor;
import com.clustercontrol.selfcheck.monitor.SnmpTrapQueueMonitor;
import com.clustercontrol.selfcheck.monitor.SyslogQueueMonitor;
import com.clustercontrol.selfcheck.monitor.TableSizeMonitor;
import com.clustercontrol.selfcheck.monitor.ThreadActivityMonitor;
import com.clustercontrol.selfcheck.monitor.WebServiceQueueMonitor;
import com.clustercontrol.selfcheck.util.SelfCheckDivergence;
import com.clustercontrol.util.HinemosTime;

/**
 * セルフチェック機能の定期実行制御クラス
 */
public class SelfCheckTaskSubmitter implements Runnable {

	private static Log log = LogFactory.getLog( SelfCheckTaskSubmitter.class );

	private final ScheduledExecutorService _scheduler;
	private final ExecutorService _executorService;
	public static volatile Date lastMonitorDate = null;

	public SelfCheckTaskSubmitter() {
		_scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "SelfCheckScheduler");
			}
		});

		_executorService = Executors.newFixedThreadPool(
				HinemosPropertyCommon.selfcheck_threadpool_size.getIntegerValue(),
				new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "SelfCheckWorker-" + _count++);
					}
				}
				);
	}

	/**
	 * セルフチェック機能を活性化させるメソッド
	 */
	@SuppressWarnings("unchecked")
	public void start() {
		_scheduler.scheduleWithFixedDelay(this
				, HinemosPropertyCommon.selfcheck_startup_delay.getNumericValue()
				, HinemosPropertyCommon.selfcheck_interval.getNumericValue(),
				TimeUnit.SECONDS);

		// evaluation key
		// 評価版のキーが使われているかのチェック
		boolean isCheckEvaluationKey = false;
		for (String option : OptionManager.getOptions()) {
			if (option.endsWith(ActivationKeyConstant.EVALUATION_SUFFIX) 
					|| option.endsWith(ActivationKeyConstant.EVALUATION_EXPIRED_SUFFIX)){
				isCheckEvaluationKey = true;
				break;
			}
		}
		
		// 評価版のキーだった場合はRAMスケジューラで定期実行
		if (isCheckEvaluationKey) {
			String scheduleName = "EvaluationKey";
			String scheduleGroup = "EVALUATION_KEY_CHECK";
			try {
				SchedulerPlugin.scheduleCronJob(
						SchedulerType.RAM_MONITOR, 
						scheduleName, 
						scheduleGroup, 
						HinemosTime.currentTimeMillis(), 
						HinemosPropertyCommon.selfcheck_evaluation_alert_cron.getStringValue(), 
						true, 
						ActivationKeyMonitor.class.getName(), 
						"execute", 
						new Class[]{}, new Serializable[]{}
				);
				log.trace("evaluation key check scheduled in cron \"" + HinemosPropertyCommon.selfcheck_evaluation_alert_cron.getStringValue() + "\"");
			} catch (HinemosUnknown e) {
				log.warn("evaluation key check failure.", e);
			}
		}
	}

	/**
	 * セルフチェック機能を非活性化させるメソッド
	 */
	public void shutdown() {
		// キック元となるスケジューラから停止していく
		_scheduler.shutdown();
		long _shutdownTimeoutMsec = HinemosPropertyCommon.hinemos_selfcheck_shutdown_timeout.getNumericValue();

		try {
			if (! _scheduler.awaitTermination(_shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _scheduler.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_scheduler.shutdownNow();
		}

		_executorService.shutdown();
		try {
			if (! _executorService.awaitTermination(_shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				_executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			_executorService.shutdownNow();
		}
	}

	/**
	 * 定期実行間隔(interval)に基づいて、定期的に実行されるメソッド
	 */
	@Override
	public void run() {
		/** メイン処理 */
		// Java VM Heap
		_executorService.submit(new SelfCheckTask(new JVMHeapMonitor()));

		// FileSystem
		_executorService.submit(new SelfCheckTask(new FileSystemMonitor()));

		// swap-out
		_executorService.submit(new SelfCheckTask(new RAMSwapOutMonitor()));

		// Database
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			_executorService.submit(new SelfCheckTask(new DatabaseMonitor()));
		}

		// DB Long Transaction
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			_executorService.submit(new SelfCheckTask(new DBLongTranMonitor()));
		}
		
		// DB Connection Count
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			_executorService.submit(new SelfCheckTask(new DBConnectionCountMonitor()));
		}

		// Scheduler
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			try {
				List<SchedulerInfo> triggerList = SchedulerPlugin.getSchedulerList(com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType.DBMS_JOB);
				for (SchedulerInfo trigger : triggerList) {
					if ( !(trigger.isPaused) ) {
						_executorService.submit(
								new SelfCheckTask(
										new SchedulerMonitor(
												com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType.DBMS_JOB,
												trigger
												)
										)
								);
					}
					if( log.isTraceEnabled() ){
						log.trace("run(): DBMS_JOB trigger.name="+trigger.name +", trigger.isPaused="+trigger.isPaused);
					}
				}
				if(log.isTraceEnabled() && triggerList != null){
					log.trace("run(): DBMS_JOB getSchedulerList().size()="+triggerList.size());
				}
			} catch (Exception e) {
				log.warn("quartz scheduler access failure. (" + SchedulerType.DBMS_JOB + ")", e);
			}
			
			try {
				List<SchedulerInfo> triggerList = SchedulerPlugin.getSchedulerList(com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType.DBMS_DEL);
				for (SchedulerInfo trigger : triggerList) {
					if ( !(trigger.isPaused) ) {
						_executorService.submit(
								new SelfCheckTask(
										new SchedulerMonitor(
												com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType.DBMS_DEL,
												trigger
												)
										)
								);
					}
					if( log.isTraceEnabled() ){
						log.trace("run(): DBMS_DEL trigger.name="+trigger.name +", trigger.isPaused="+trigger.isPaused );
					}
				}
				if(log.isTraceEnabled() && triggerList != null){
					log.trace("run(): DBMS_DEL getSchedulerList().size()="+triggerList.size());
				}
			} catch (Exception e) {
				log.warn("quartz scheduler access failure. (" + SchedulerType.DBMS_DEL + ")", e);
			}
			
			try {
				List<SchedulerInfo> triggerList = SchedulerPlugin.getSchedulerList(com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType.DBMS);
				for (SchedulerInfo trigger : triggerList) {
					if ( !(trigger.isPaused) ) {
						_executorService.submit(
								new SelfCheckTask(
										new SchedulerMonitor(
												com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType.DBMS,
												trigger
												)
										)
								);
					}
					if( log.isTraceEnabled() ){
						log.trace("run(): DBMS trigger.name="+trigger.name +", trigger.isPaused="+trigger.isPaused);
					}
				}
				if(log.isTraceEnabled() && triggerList != null){
					log.trace("run(): DBMS getSchedulerList().size()="+triggerList.size());
				}
			} catch (Exception e) {
				log.warn("quartz scheduler access failure. (" + SchedulerType.DBMS + ")", e);
			}

			try {
				List<SchedulerInfo> triggerList = SchedulerPlugin.getSchedulerList(SchedulerType.RAM_MONITOR);
				for (SchedulerInfo trigger : triggerList) {
					if ( !(trigger.isPaused) ) {
						_executorService.submit(
								new SelfCheckTask(
										new SchedulerMonitor(
												SchedulerType.RAM_MONITOR,
												trigger
												)
										)
								);
					}
					if( log.isTraceEnabled() ){
						log.trace("run(): RAM_MONITOR trigger.name="+trigger.name +", trigger.isPaused="+trigger.isPaused);
					}
				}
				if(log.isTraceEnabled() && triggerList != null){
					log.trace("run(): RAM_MONITOR getSchedulerList().size()="+triggerList.size());
				}
			} catch (Exception e) {
				log.warn("quartz scheduler access failure. (" + SchedulerType.RAM_MONITOR + ")", e);
			}
			
			try {
				List<SchedulerInfo> triggerList = SchedulerPlugin.getSchedulerList(SchedulerType.RAM_JOB);
				for (SchedulerInfo trigger : triggerList) {
					if ( !(trigger.isPaused) ) {
						_executorService.submit(
								new SelfCheckTask(
										new SchedulerMonitor(
												SchedulerType.RAM_JOB,
												trigger
												)
										)
								);
					}
					if( log.isTraceEnabled() ){
						log.trace("run(): RAM_JOB trigger.name="+trigger.name +", trigger.isPaused="+trigger.isPaused);
					}
				}
				if(log.isTraceEnabled() && triggerList != null){
					log.trace("run(): RAM_JOB getSchedulerList().size()="+triggerList.size());
				}
			} catch (Exception e) {
				log.warn("quartz scheduler access failure. (" + SchedulerType.RAM_JOB + ")", e);
			}
		}

		// Web Service
		_executorService.submit(new SelfCheckTask(new WebServiceQueueMonitor()));

		// REST Service
		_executorService.submit(new SelfCheckTask(new RestServiceQueueMonitor()));

		// syslog queue
		_executorService.submit(new SelfCheckTask(new SyslogQueueMonitor()));

		// snmptrap queue
		_executorService.submit(new SelfCheckTask(new SnmpTrapQueueMonitor()));

		// asynchronous task queue
		_executorService.submit(new SelfCheckTask(new AsyncTaskQueueMonitor()));

		// thread activity
		for (ThreadInfo threadInfo : MonitoredThreadPoolExecutor.getRunningThreadMap().values()) {
			_executorService.submit(new SelfCheckTask(new ThreadActivityMonitor(threadInfo)));
		}

		// log table
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			_executorService.submit(new SelfCheckTask(new TableSizeMonitor()));
		}

		// job
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			_executorService.submit(new SelfCheckTask(new JobRunSessionMonitor()));
		}
		
		// platform
		SelfCheckMonitor[] platformMonitors = SelfCheckDivergence.getMonitors(HinemosManagerMain._startupMode);
		for (SelfCheckMonitor monitor : platformMonitors) {
			_executorService.submit(new SelfCheckTask(monitor));
		}
		
		// job queue
		_executorService.submit(new SelfCheckTask(new JobQueueMonitor()));
		
		// set timestamp of last monitoring
		refreshMonitorDate();
	}
	
	private static void refreshMonitorDate() {
		lastMonitorDate = HinemosTime.getDateInstance();
	}

}
