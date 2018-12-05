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

package com.clustercontrol.plugin.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.quartz.job.ReflectionInvokerJob;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.LogTransferNotFound;
import com.clustercontrol.hub.bean.PropertyConstants;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.model.TransferInfo.TransferType;
import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.HinemosManagerMain.StartupTask;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.factory.ModifySchedule;
import com.clustercontrol.monitor.run.util.NodeMonitorPollerController;
import com.clustercontrol.monitor.run.util.NodeToMonitorCache;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.factory.ModifyDbmsScheduler;
import com.clustercontrol.plugin.model.DbmsSchedulerEntity;
import com.clustercontrol.plugin.util.scheduler.AbstractTrigger;
import com.clustercontrol.plugin.util.scheduler.CronExpression;
import com.clustercontrol.plugin.util.scheduler.CronTriggerBuilder;
import com.clustercontrol.plugin.util.scheduler.HinemosScheduler;
import com.clustercontrol.plugin.util.scheduler.JobBuilder;
import com.clustercontrol.plugin.util.scheduler.JobDetail;
import com.clustercontrol.plugin.util.scheduler.JobKey;
import com.clustercontrol.plugin.util.scheduler.SchedulerException;
import com.clustercontrol.plugin.util.scheduler.SimpleTriggerBuilder;
import com.clustercontrol.plugin.util.scheduler.Trigger;
import com.clustercontrol.plugin.util.scheduler.TriggerState;
import com.clustercontrol.plugin.util.QueryUtil;
import com.clustercontrol.repository.session.RepositoryRunManagementBean;
import com.clustercontrol.util.HinemosTime;

/**
 * 内部スケジューラを管理するプラグインサービス
 */
public class SchedulerPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(SchedulerPlugin.class);

	// スケジュール情報の登録方法(CRON : cronと同様の書式を指定, SIMPLE : INTERVALのみを指定, NONE : トリガーを登録しない )
	public static enum TriggerType { CRON, SIMPLE, NONE };

	// スケジューラ情報の保持種別(RAM : オンメモリで管理、DBMS : DBで永続化管理)
	public static enum SchedulerType {
		RAM( "ram",  "scheduler.ram.threadPool.size", 32, "scheduler.ram.misfireThreshold",  3600000),
		DBMS("dbms", "scheduler.dbms.threadPool.size", 8, "scheduler.dbms.misfireThreshold", 3600000);
		
		SchedulerType(String name, String poolSizeKey, int defaultPoolSize, String misfireThresholdKey, int defaultMisfireThreashold) {
			mainThreadName = "HinemosScheduler-" + name + "-dispatcher";
			workerThreadNameBase = "HinemosScheduler-" + name + "-worker-";
			poolSize = HinemosPropertyUtil.getHinemosPropertyNum(poolSizeKey, Long.valueOf(defaultPoolSize)).intValue();
			misfireThreshold = HinemosPropertyUtil.getHinemosPropertyNum(misfireThresholdKey, Long.valueOf(defaultMisfireThreashold)).intValue();
		}
		private final String mainThreadName;
		public String getMainThreadName() {
			return mainThreadName; 
		}
		private final String workerThreadNameBase;
		public String getWorkerThreadNameBase() {
			return workerThreadNameBase;
		}
		private final int poolSize;
		public int getPoolSize() {
			return poolSize;
		}
		private final int misfireThreshold;
		public int getMisfireThreshold() {
			return misfireThreshold;
		}
		
	};

	private static final Object _schedulerLock = new Object();
	private static final Map<SchedulerType, HinemosScheduler> _scheduler = new ConcurrentHashMap<SchedulerType, HinemosScheduler>(2);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(WebServiceStartHTTPSPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
		try {
			synchronized (_schedulerLock) {
				int delaySec = HinemosPropertyUtil.getHinemosPropertyNum("common.scheduler.startup.delay", Long.valueOf(60)).intValue();
				log.info("initializing SchedulerPlugin : properties (delaySec = " + delaySec + ")");
				
				HinemosScheduler ram = new HinemosScheduler(SchedulerType.RAM);
				HinemosScheduler dbms = new HinemosScheduler(SchedulerType.DBMS);
				_scheduler.put(SchedulerType.RAM, ram);
				_scheduler.put(SchedulerType.DBMS, dbms);
			}
			
			if (HinemosManagerMain._startupMode != StartupMode.MAINTENANCE) {
				initTrigger();
			}
		} catch (com.clustercontrol.plugin.util.scheduler.SchedulerException e) {
			log.error("initialization failure : SchedulerPlugin", e);
		} catch (HinemosException e) {
			log.error("initialization failure : SchedulerPlugin", e);
		}
	}

	@Override
	public void destroy() {


	}

	@Override
	public void activate() {
		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			log.info("skipped activation (startup mode is MAINTENANCE) : SchedulerPlugin");
			HinemosManagerMain.addStartupTask(new SchedulerStartupTask(this));
			return;
		}

		// Hinemos時刻(スケジューラが管理している現在時刻)の設定は、HinemosManagerMainでのpluginサービス起動前に事前に行うこと。
		
		for (Entry<SchedulerType, HinemosScheduler> entry : _scheduler.entrySet()) {
			try {
				log.debug("activate scheduler name=" + entry.getValue().getSchedulerName());
				int delaySec = HinemosPropertyUtil.getHinemosPropertyNum("common.scheduler.startup.delay", Long.valueOf(60)).intValue();
				entry.getValue().start(delaySec * 1000);
			} catch (SchedulerException e) {
				log.error("activation failure : SchedulerPlugin", e);
			}
		}

	}

	public static class SchedulerStartupTask implements StartupTask {
		
		private final SchedulerPlugin _plugin;
		
		public SchedulerStartupTask(SchedulerPlugin plugin) {
			_plugin = plugin;
		}
		
		@Override
		public void init() {
			try {
				_plugin.initTrigger();
			} catch (HinemosException e) {
				log.error("initialization failure : SchedulerPlugin", e);
			}
			
			// Hinemos時刻(スケジューラが管理している現在時刻)の設定は、HinemosManagerMainでのpluginサービス起動前に事前に行うこと。
			
			for (Entry<SchedulerType, HinemosScheduler> entry : _scheduler.entrySet()) {
				try {
					log.info("activate scheduler name=" + entry.getValue().getSchedulerName());
					entry.getValue().start(0);
				} catch (SchedulerException e) {
					log.error("activation failure : SchedulerPlugin", e);
				}
			}
		}
		
	}
	
	@Override
	public void deactivate() {
		if (HinemosManagerMain._startupMode == StartupMode.NORMAL) {
			new MonitorControllerBean().persistMonitorStatusCache();
		}

		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			log.info("skipped deactivation (startup mode is MAINTENANCE) : SchedulerPlugin");
			return;
		}

		for (Entry<SchedulerType, HinemosScheduler> entry : _scheduler.entrySet()) {
			try {
				log.debug("shutdown scheduler name=" + entry.getValue().getSchedulerName());
				entry.getValue().shutdown();
			} catch (SchedulerException e) {
				log.error("shutdown failure : SchedulerPlugin", e);
			}
		}
	}
	
	/**
	 * <pre>
	 * 単に定期実行するだけのジョブをスケジューリングするためのメソッド。<br/>
	 * ユーザは実行周期のみを定義可能であり、cronのように具体的な実行タイミングを定義できない。<br/>
	 * </pre>
	 *
	 * @param type スケジューラ定義の保持型
	 * @param name ジョブの名前
	 * @param group ジョブのグループ名
	 * @param startTime 実行開始日時
	 * @param intervalSec 実行間隔[sec]
	 * @param rstOnRestart JVM再起動時に実行開始日時をリセットする場合はtrue(Misfire時間内に実行予定となっていたジョブを繰り返し実行せずに、現在時刻以降の実行予定から開始する）
	 * @param className ジョブが実装されたクラス名
	 * @param methodName ジョブが実装されたメソッド名
	 * @param argsType メソッドの引数型配列
	 * @param args メソッドの引数配列
	 * @throws HinemosUnknown
	 */
	public static void scheduleSimpleJob(SchedulerType type, String name, String group,
			long startTimeMillis, int intervalSec, boolean rstOnRestart,
			String className, String methodName, Class<? extends Serializable>[] argsType, Serializable[] args) throws HinemosUnknown {

		log.debug("scheduleSimpleJob() name=" + name + ", group=" + group + ", startTime=" + startTimeMillis
				+ ", rstOnRestart=" + rstOnRestart + ", className=" + className + ", methodName=" + methodName);

		// ジョブ定義の作成
		JobDetail job = JobBuilder.newJob(ReflectionInvokerJob.class)
				.withIdentity(name, group)
				.storeDurably(true)		// ジョブ完了時に削除されない設定を反映
//				.requestRecovery(false)	// ジョブ実行が失敗した際に再実行しない設定を反映(JVM起動中に再実行が繰り返される可能性を回避するため)
				.usingJobData(ReflectionInvokerJob.KEY_CLASS_NAME, className)	// ジョブから呼び出すクラス名を反映
				.usingJobData(ReflectionInvokerJob.KEY_METHOD_NAME, methodName)	// ジョブから呼び出すメソッドを反映
				.usingJobData(ReflectionInvokerJob.KEY_RESET_ON_RESTART, rstOnRestart)	// 再起動時にtriggerをリセット()するかどうかを反映
				.build();

		// [WARNING] job.getJobDataMap()ではなく、"trigger".getJobDataMap()に対して値を定義してはいけない。
		// Quartz (JBoss EAP 5.1 Bundle) Bugにより、java.lang.StackOverflowErrorの発生を引き起こす。

		// メソッドの引数を定義する（引数無は0-lengthの配列とする)
		if (args == null) {
			throw new NullPointerException("args must not be null. if not args, set 0-length list.");
		}
		if (argsType == null) {
			throw new NullPointerException("argsType must not be null. if not args, set 0-length list.");
		}
		if (args.length != argsType.length) {
			throw new IndexOutOfBoundsException("list's length is not same between args and argsType.");
		}
		if (args.length > 15) {
			throw new IndexOutOfBoundsException("list's length is out of bounds.");
		}
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS_TYPE, argsType);
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS, args);

		// ジョブ実行定義となるtriggerを作成
		SimpleTriggerBuilder triggerBuilder = SimpleTriggerBuilder.newTrigger().withIdentity(name, group);
		if (rstOnRestart) {
			log.debug("scheduleSimpleJob() name=" + name + ", misfireHandlingInstruction=DoNothing");
			triggerBuilder.setPeriod(intervalSec * 1000).withMisfireHandlingInstructionDoNothing();
		} else {
			log.debug("scheduleSimpleJob() name=" + name + ", misfireHandlingInstruction=IgnoreMisfires");
			triggerBuilder.setPeriod(intervalSec * 1000).withMisfireHandlingInstructionIgnoreMisfires();
		}

		Trigger trigger = triggerBuilder
				.startAt(startTimeMillis)
//				.withSchedule(scheduleBuilder)
				.build();
		
		if(type == SchedulerPlugin.SchedulerType.DBMS){
			// DBMSスケジューラの場合、存在チェックの上、DBへ登録または更新処理を呼ぶ
			// 同じレコードへの削除/登録は1トランザクションでは連続で出来ないため
			try {
				ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
				if (_scheduler.get(type).checkExists(new JobKey(name, group))) {
					// 登録済みの場合は、DB側は更新処理を呼ぶ
					log.trace("scheduleSimpleJob() : modifyDbmsScheduler() call.");
					dbms.modifyDbmsScheduler(job, trigger);
					
					synchronized (_schedulerLock) {
						// rescheduleJob()ではTrigger情報のみしか更新しないため、RAM側は再登録処理が必要
						_scheduler.get(type).deleteJob(new JobKey(name, group));
						log.debug("scheduleJob() name=" + name + ", group=" + group);
						_scheduler.get(type).scheduleJob(job, trigger);
					}
				} else {
					// 未登録の場合は、DB登録処理を呼ぶ
					log.trace("scheduleSimpleJob() : addDbmsScheduler() call.");
					dbms.addDbmsScheduler(job, trigger);
					
					synchronized (_schedulerLock) {
						log.debug("scheduleJob() name=" + name + ", group=" + group);
						_scheduler.get(type).scheduleJob(job, trigger);
					}
				}
			} catch (Exception e) {
				log.error("scheduleSimpleJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown("failed scheduling DBMS job. (name = " + name + ", group = " + group + ")", e);
			}
		} else {
			// RAMスケジューラの場合は、存在有無に関わらず削除処理を呼ぶ
			deleteJob(type, name, group);
			// ジョブスケジューラを作成
			try {
				synchronized (_schedulerLock) {
					log.debug("scheduleJob() name=" + name + ", group=" + group);
					_scheduler.get(type).scheduleJob(job, trigger);
				}
			} catch (SchedulerException e) {
				throw new HinemosUnknown("failed scheduling job. (name = " + name + ", group = " + group + ")", e);
			}
		}
	}

	public static void scheduleCronJob(SchedulerType type, String name, String group,
			long startTime, String cronExpression, boolean rstOnRestart,
			String className, String methodName, Class<? extends Serializable>[] argsType, Serializable[] args) throws HinemosUnknown {

		log.debug("scheduleCronJob() name=" + name + ", group=" + group + ", startTime=" + startTime + ", cronExpression=" + cronExpression
				+ ", rstOnRestart=" + rstOnRestart + ", className=" + className + ", methodName=" + methodName);

		// ジョブ定義の作成
		JobDetail job = JobBuilder.newJob(ReflectionInvokerJob.class)
				.withIdentity(name, group)
				.storeDurably(true)		// ジョブ完了時に削除されない設定を反映
//				.requestRecovery(false)	// ジョブ実行が失敗した際に再実行しない設定を反映(JVM起動中に再実行が繰り返される可能性を回避するため)
				.usingJobData(ReflectionInvokerJob.KEY_CLASS_NAME, className)	// ジョブから呼び出すクラス名を反映
				.usingJobData(ReflectionInvokerJob.KEY_METHOD_NAME, methodName)	// ジョブから呼び出すメソッドを反映
				.usingJobData(ReflectionInvokerJob.KEY_RESET_ON_RESTART, rstOnRestart)	// 再起動時にtriggerをリセット()するかどうかを反映
				.build();

		// [WARNING] job.getJobDataMap()ではなく、"trigger".getJobDataMap()に対して値を定義してはいけない。
		// Quartz (JBoss EAP 5.1 Bundle) Bugにより、java.lang.StackOverflowErrorの発生を引き起こす。

		// メソッドの引数を定義する（引数無は0-lengthの配列とする)
		if (args == null) {
			throw new NullPointerException("args must not be null. if not args, set 0-length list.");
		}
		if (argsType == null) {
			throw new NullPointerException("argsType must not be null. if not args, set 0-length list.");
		}
		if (args.length != argsType.length) {
			throw new IndexOutOfBoundsException("list's length is not same between args and argsType.");
		}
		if (args.length > 15) {
			throw new IndexOutOfBoundsException("list's length is out of bounds.");
		}
		
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS_TYPE, argsType);
		job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS, args);

		// ジョブ実行定義となるtriggerを作成
		CronTriggerBuilder triggerBuilder = CronTriggerBuilder.newTrigger();
		if (rstOnRestart) {
			log.debug("scheduleCronJob() name=" + name + ", misfireHandlingInstruction=DoNothing");
			triggerBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
		} else {
			log.debug("scheduleCronJob() name=" + name + ", misfireHandlingInstruction=IgnoreMisfires");
			triggerBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionIgnoreMisfires();
		}

		Trigger trigger = triggerBuilder
				.withIdentity(name, group)
				.startAt(startTime)
//				.withSchedule(schedulerBuilder)
				.build();
		
		if(type == SchedulerPlugin.SchedulerType.DBMS){
			// DBMSスケジューラの場合、存在チェックの上、DBへ登録または更新処理を呼ぶ
			// 同じレコードへの削除/登録は1トランザクション内では出来ないため
			try {
				ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
				if (_scheduler.get(type).checkExists(new JobKey(name, group))) {
					// 登録済みの場合は、DB側は更新処理を呼ぶ
					log.trace("scheduleCronJob() : modifyDbmsScheduler() call.");
					dbms.modifyDbmsScheduler(job, trigger);
					
					synchronized (_schedulerLock) {
						// rescheduleJob()ではTrigger情報のみしか更新しないため、RAM側は再登録処理が必要
						_scheduler.get(type).deleteJob(new JobKey(name, group));
						log.debug("scheduleJob() name=" + name + ", group=" + group);
						_scheduler.get(type).scheduleJob(job, trigger);
					}
				} else {
					// 未登録の場合は、DB登録処理を呼ぶ
					log.trace("scheduleCronJob() : addDbmsScheduler() call.");
					dbms.addDbmsScheduler(job, trigger);
					
					synchronized (_schedulerLock) {
						log.debug("scheduleJob() name=" + name + ", group=" + group);
						_scheduler.get(type).scheduleJob(job, trigger);
					}
				}
			} catch (Exception e) {
				log.error("scheduleCronJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown("failed scheduling DBMS job. (name = " + name + ", group = " + group + ")", e);
			}
		} else {
			// RAMスケジューラの場合は、存在有無に関わらず削除処理を呼ぶ
			deleteJob(type, name, group);
			
			// ジョブスケジューラを作成
			try {
				synchronized (_schedulerLock) {
					log.debug("scheduleJob() name=" + name + ", group=" + group);
					_scheduler.get(type).scheduleJob(job, trigger);
				}
			} catch (SchedulerException e) {
				throw new HinemosUnknown("failed scheduling job. (name = " + name + ", group = " + group + ")", e);
			}
		}
	}

	/**
	 * <pre>
	 * 既にジョブが登録されている場合、そのジョブを削除する。<br/>
	 * (APIの仕様上、未登録の場合はfalseが返されるだけで例外は生じない)</br>
	 * </pre>
	 * @param type スケジューラ定義の保持型
	 * @param name ジョブの名前
	 * @param group ジョブのグループ名
	 * @throws HinemosUnknown 予期せぬ内部エラー
	 */
	public static void deleteJob(SchedulerType type, String name, String group) throws HinemosUnknown {
		if (log.isDebugEnabled()) log.debug("deleteJob() name:" + name + ", group:" + group);
		
		if(type == SchedulerPlugin.SchedulerType.DBMS){
			try {
				log.trace("deleteJob() : deleteDbmsScheduler() call.");
				ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
				dbms.deleteDbmsScheduler(name, group);
			} catch (Exception e){
				throw new HinemosUnknown("failed removing DBMS job. (name = " + name + ", group = " + group + ")", e);
			}
		}
		
		try {
			synchronized (_schedulerLock) {
				_scheduler.get(type).deleteJob(new JobKey(name, group));
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed removing job. (name = " + name + ", group = " + group + ")", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void initTrigger() throws HinemosUnknown {
		// setup Job for Status Notification Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("MonitorController", "MON"))) {
				scheduleCronJob(SchedulerType.RAM, "MonitorController", "MON",
						HinemosTime.currentTimeMillis(), HinemosPropertyUtil.getHinemosPropertyStr(
								"scheduler.monitor.cron", "0 */5 * * * ? *"),
						true, MonitorControllerBean.class.getName(),
						"manageStatus", new Class[0], new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = MonitorController, group = MON)", e);
		}

		// setup Job for Job Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("JobRunManagement", "JOB_MANAGEMENT"))) {
				scheduleCronJob(SchedulerType.RAM, "JobRunManagement",
						"JOB_MANAGEMENT", HinemosTime.currentTimeMillis(),
						HinemosPropertyUtil.getHinemosPropertyStr("scheduler.job.cron",
								"0 */1 * * * ? *"), true,
						JobRunManagementBean.class.getName(), "run",
						new Class[0], new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = JobRunManagement, group = JOB_MANAGEMENT)", e);
		}

		// setup Job for Repository Run Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("RepositoryRunManagement", "REPOSITORY_MANAGEMENT"))) {
				scheduleCronJob(SchedulerType.RAM, "RepositoryRunManagement",
						"REPOSITORY_MANAGEMENT", HinemosTime.currentTimeMillis(),
						HinemosPropertyUtil.getHinemosPropertyStr("scheduler.repository.cron",
								"40 */1 * * * ? *"), true,
						RepositoryRunManagementBean.class.getName(), "run",
						new Class[0], new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = RepositoryRunManagement, group = REPOSITORY_MANAGEMENT)", e);
		}

		// setup Job for Monitor Status Management
		try {
			if (! _scheduler.get(SchedulerType.RAM).checkExists(new JobKey("MonitorStatusManagement", "MONITOR_STATUS_MANAGEMENT"))) {
				scheduleCronJob(SchedulerType.RAM, "MonitorStatusManagement",
						"MONITOR_STATUS_MANAGEMENT", HinemosTime.currentTimeMillis(),
						HinemosPropertyUtil.getHinemosPropertyStr(
								"scheduler.monitor.status.cron",
								"50 3/10 * * * ? *"), true,
						MonitorControllerBean.class.getName(),
						"persistMonitorStatusCache", new Class[0],
						new Serializable[0]);
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed initializing job. (name = MonitorStatusManagement, group = MONITOR_STATUS_MANAGEMENT)", e);
		}

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// リソース監視・プロセス監視について、Nodeから監視項目への辞書作成を行なう
			NodeToMonitorCache.getInstance(HinemosModuleConstant.MONITOR_PROCESS).refresh();
			NodeToMonitorCache.getInstance(HinemosModuleConstant.MONITOR_PERFORMANCE).refresh();
			
			// リソース監視・プロセス監視のノード単位のポーラーをスケジュールする
			NodeMonitorPollerController.init();
			
			// 監視をリスケジューリングする。
			new ModifySchedule().updateScheduleAll();

			initDbmsScheduler();
			
			jtm.commit();
		} catch (Exception e) {
			if (jtm != null)
				jtm.rollback();
			log.warn("failed to start schedulers.", e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	private void initDbmsScheduler() throws HinemosUnknown {
		log.debug("initDbmsScheduler() start.");
		Throwable exception = null;
		
		//収集蓄積機能の対応(転送設定に関するHinemosプロパティの最新値を取得)
		String propCron = null;
		String propBaseTime= null;
		propCron = PropertyConstants.hub_transfer_delay_interval.string();
		log.debug("propCron:" + propCron);
		try {
			CronExpression.validateExpression(propCron);
		} catch (ParseException e){
			log.warn(PropertyConstants.hub_transfer_delay_interval.message_invalid(propCron));
			//書式不正の場合、DB情報更新しない
			propCron = null;
		}
		propBaseTime = PropertyConstants.hub_transfer_batch_basetime.string();
		log.debug("propBaseTime:" + propBaseTime);
		
		
		List<DbmsSchedulerEntity> entityList = null;
		entityList = QueryUtil.getAllDbmsScheduler();
		
		for (DbmsSchedulerEntity entity : entityList) {
			if (log.isDebugEnabled()) log.debug("entity:" + entity.getId().getJobId());
			try {
				boolean isMisfire = entity.getMisfireInstr() == Trigger.MISFIRE_INSTRUCTION_DO_NOTHING ? true : false;
				
				//メソッドの引数を設定
				Serializable[] jdArgs = new Serializable[entity.getJobArgNum()];
				@SuppressWarnings("unchecked")
				Class<? extends Serializable>[] jdArgsType = new Class[entity.getJobArgNum()];
				
				for (int i=0; i < entity.getJobArgNum(); i++){
					
					String strArg = null;
					
					switch (i) {
					case 0  : strArg = entity.getJobArg00(); break;
					case 1  : strArg = entity.getJobArg01(); break;
					case 2  : strArg = entity.getJobArg02(); break;
					case 3  : strArg = entity.getJobArg03(); break;
					case 4  : strArg = entity.getJobArg04(); break;
					case 5  : strArg = entity.getJobArg05(); break;
					case 6  : strArg = entity.getJobArg06(); break;
					case 7  : strArg = entity.getJobArg07(); break;
					case 8  : strArg = entity.getJobArg08(); break;
					case 9  : strArg = entity.getJobArg09(); break;
					case 10 : strArg = entity.getJobArg10(); break;
					case 11 : strArg = entity.getJobArg11(); break;
					case 12 : strArg = entity.getJobArg12(); break;
					case 13 : strArg = entity.getJobArg13(); break;
					case 14 : strArg = entity.getJobArg14(); break;
					default: log.debug("arg count ng.");
					}
					
					if (log.isDebugEnabled()) log.debug("strArg[" + i + "]:" + strArg);
					
					if (strArg != null) {
						String[] splitArg = strArg.split(":", 2);
						
						if (splitArg[0].equals("String")){
							jdArgsType[i] = String.class;
							jdArgs[i] = splitArg[1];
						} else if (splitArg[0].equals("Boolean")){
							jdArgsType[i] = Boolean.class;
							jdArgs[i] = Boolean.parseBoolean(splitArg[1]);
						} else if (splitArg[0].equals("Integer")){
							jdArgsType[i] = Integer.class;
							jdArgs[i] = Integer.parseInt(splitArg[1]);
						} else if (splitArg[0].equals("Long")){
							jdArgsType[i] = Long.class;
							jdArgs[i] = Long.parseLong(splitArg[1]);
						} else if (splitArg[0].equals("Short")){
							jdArgsType[i] = Short.class;
							jdArgs[i] = Short.parseShort(splitArg[1]);
						} else if (splitArg[0].equals("Float")){
							jdArgsType[i] = Float.class;
							jdArgs[i] = Float.parseFloat(splitArg[1]);
						} else if (splitArg[0].equals("Double")){
							jdArgsType[i] = Double.class;
							jdArgs[i] = Double.parseDouble(splitArg[1]);
						} else if (splitArg[0].equals("nullString")){
							jdArgsType[i] = String.class;
							jdArgs[i] = null;
						} else {
							log.debug("not support class");
						}
						if (log.isDebugEnabled()) log.debug("jdArgs[" + i + "]:" + jdArgs[i]);
					}
				}
				// ジョブ定義の作成
				JobDetail job = JobBuilder.newJob(ReflectionInvokerJob.class)
						.withIdentity(entity.getId().getJobId(), entity.getId().getJobGroup())
						.storeDurably(true)		// ジョブ完了時に削除されない設定を反映
						.usingJobData(ReflectionInvokerJob.KEY_CLASS_NAME, entity.getJobClassName())	// ジョブから呼び出すクラス名を反映
						.usingJobData(ReflectionInvokerJob.KEY_METHOD_NAME, entity.getJobMethodName())	// ジョブから呼び出すメソッドを反映
						.usingJobData(ReflectionInvokerJob.KEY_RESET_ON_RESTART, isMisfire)	// 再起動時にtriggerをリセット()するかどうかを反映
						.build();
				
				job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS_TYPE, jdArgsType);
				job.getJobDataMap().put(ReflectionInvokerJob.KEY_ARGS, jdArgs);
				
				// ジョブ実行定義となるtriggerを作成
				Trigger trigger = null;
				if(entity.getTriggerType().equals(SchedulerPlugin.TriggerType.CRON.name())){
					CronTriggerBuilder triggerBuilder = CronTriggerBuilder.newTrigger().withIdentity(entity.getId().getJobId(), entity.getId().getJobGroup());
					if (isMisfire) {
						triggerBuilder.withMisfireHandlingInstructionDoNothing();
					} else {
						triggerBuilder.withMisfireHandlingInstructionIgnoreMisfires();
					}
					// 現在時刻を一度ミリ秒単位を落として取得する
					long nowTime = (HinemosTime.currentTimeMillis() / 1000) * 1000;
					log.debug("start_time : " + nowTime);
					trigger = triggerBuilder.cronSchedule(entity.getCronExpression()).startAt(nowTime).endAt(entity.getEndTime()).build();
					((AbstractTrigger)trigger).setNextFireTime(entity.getNextFireTime());
					((AbstractTrigger)trigger).setPreviousFireTime(entity.getPrevFireTime());
					
				} else if (entity.getTriggerType().equals(SchedulerPlugin.TriggerType.SIMPLE.name())){
					SimpleTriggerBuilder triggerBuilder = SimpleTriggerBuilder.newTrigger().withIdentity(entity.getId().getJobId(), entity.getId().getJobGroup());
					if (isMisfire) {
						triggerBuilder.withMisfireHandlingInstructionDoNothing();
					} else {
						triggerBuilder.withMisfireHandlingInstructionIgnoreMisfires();
					}
					trigger = triggerBuilder.setPeriod(entity.getRepeatInterval()).startAt(entity.getStartTime()).endAt(entity.getEndTime()).build();
					((AbstractTrigger)trigger).setNextFireTime(entity.getNextFireTime());
					((AbstractTrigger)trigger).setPreviousFireTime(entity.getPrevFireTime());
				}
				
				//収集蓄積機能の対応(転送設定に関するHinemosプロパティの情報を反映)
				if(entity.getId().getJobGroup().equals(com.clustercontrol.hub.bean.QuartzConstant.GROUP_NAME)) {
					//収集蓄積のDB設定情報を取得
					TransferInfo ti = null;
					try {
						ti = com.clustercontrol.hub.util.QueryUtil.getTransferInfo(entity.getId().getJobId(), ObjectPrivilegeMode.MODIFY);
					} catch (LogTransferNotFound e){
						log.warn("Not Found TransferInfo:" + entity.getId().getJobId());
					} catch (InvalidRole e){
						log.warn("Failed to get TransferInfo for InvalidRole:" + entity.getId().getJobId());
					}
					//収集蓄積の設定情報を元にtrigger情報生成
					if(ti != null){
						if(propCron != null && ti.getTransType() == TransferType.delay){
							CronTriggerBuilder triggerBuilder = CronTriggerBuilder.newTrigger().withIdentity(entity.getId().getJobId(), entity.getId().getJobGroup());
							if (isMisfire) {
								triggerBuilder.withMisfireHandlingInstructionDoNothing();
							} else {
								triggerBuilder.withMisfireHandlingInstructionIgnoreMisfires();
							}
							trigger = triggerBuilder.cronSchedule(propCron).startAt(HinemosTime.currentTimeMillis() + 15 * 1000).build();
						}else if(propBaseTime != null && ti.getTransType() == TransferType.batch){
							
							int intervalSec = ti.getInterval() * 60 * 60;
							long baseTime = com.clustercontrol.hub.factory.ModifySchedule.getBaseTime(propBaseTime, intervalSec);
							log.debug("baseTime:" + baseTime);
							SimpleTriggerBuilder triggerBuilder = SimpleTriggerBuilder.newTrigger().withIdentity(entity.getId().getJobId(), entity.getId().getJobGroup());
							if (isMisfire) {
								triggerBuilder.withMisfireHandlingInstructionDoNothing();
							} else {
								triggerBuilder.withMisfireHandlingInstructionIgnoreMisfires();
							}
							trigger = triggerBuilder.setPeriod(intervalSec * 1000).startAt(baseTime).build();
						}
					}
					//スケジューラのDB情報を更新
					try {
						ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
						dbms.modifyDbmsScheduler(job, trigger);
						trigger.computeFirstFireTime(HinemosTime.currentTimeMillis());
						dbms.modifyDbmsSchedulerInternal(job, trigger, TriggerState.SCHEDULED.name());
					} catch (Exception e){
						log.warn("hub setting init err. entity : jobId = " + entity.getId().getJobId()
								+ ", " + entity.getId().getJobGroup()
								+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
						exception = e;
					}
				}
				
				if (log.isDebugEnabled()) {
					log.debug("getNextFireTime=" + trigger.getNextFireTime() + ", getPreviousFireTime=" + trigger.getPreviousFireTime());
				}
				// ジョブスケジューラを作成
				synchronized (_schedulerLock) {
					if (log.isDebugEnabled()) log.debug("scheduleJob() name=" + entity.getId().getJobId() + ", group=" + entity.getId().getJobGroup());
					// DBには登録済みのため、RAMへの展開/登録のみ実施する関数を呼ぶ
					_scheduler.get(SchedulerPlugin.SchedulerType.DBMS).initDbmsScheduleJob(job, trigger, entity.getTriggerState());
				}
			} catch (RuntimeException e) {
				log.warn("initDbmsScheduler() entity : jobId = " + entity.getId().getJobId()
						+ ", " + entity.getId().getJobGroup()
						+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
				// 次の設定を処理するため、throwはしない。
				exception = e;
			}
		}
		
		if (exception != null) {
			throw new HinemosUnknown("failed dbms scheduler:", exception);
		}
		log.debug("initDbmsScheduler() end.");
	}
	

	public static List<SchedulerInfo> getSchedulerList(SchedulerType type) throws HinemosUnknown {
		List<SchedulerInfo> list = new ArrayList<SchedulerInfo>();

		try {
			synchronized (_schedulerLock) {
				for (Map.Entry<JobKey, Trigger> entry : _scheduler.get(type).getAllTrigger().entrySet()) {
					JobKey key = entry.getKey();
					Trigger trigger = entry.getValue();
					list.add(new SchedulerInfo(key.getName(), key.getGroup(),
							trigger.getStartTime(), trigger.getPreviousFireTime(), trigger.getNextFireTime(),
							_scheduler.get(type).getTriggerState(key) == TriggerState.PAUSED ? true : false));
				}
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed getting list of scheduled jobs.", e);
		}

		return Collections.unmodifiableList(list);
	}

	public static String schedulerSummary(SchedulerType type) {
		synchronized (_schedulerLock) {
			return "QueueSize=" + _scheduler.get(type).getQueueSize() + ", TriggerSize=" + _scheduler.get(type).getTriggerSize();
		}
	}
	
	public static long getNextFireTime(SchedulerType type, String name, String group) throws HinemosUnknown {
		long nextFireTime = -1;

		try {
			synchronized (_schedulerLock) {
				Trigger trigger = _scheduler.get(type).getTrigger(name, group);
				nextFireTime = trigger.getNextFireTime();
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed getting next fire time.", e);
		}

		log.debug("getNextFireTime() : " + nextFireTime);
		return nextFireTime;
	}

	public static boolean isSchedulerRunning(SchedulerType type) throws HinemosUnknown {
		try {
			synchronized (_schedulerLock) {
				return _scheduler.get(type).isShutdown() ? false : true;
			}
		} catch (SchedulerException e) {
			throw new HinemosUnknown("failed getting state of scheduler.", e);
		}
	}
}
