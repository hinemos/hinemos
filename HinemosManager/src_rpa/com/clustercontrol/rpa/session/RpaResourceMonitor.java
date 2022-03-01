/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.session;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.rpa.factory.RepositoryUpdater;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

public class RpaResourceMonitor {
	private static final Logger m_log = Logger.getLogger(RpaResourceMonitor.class);
	private static final String jobName = "AutoDetection";
	private static final String jobGroupName = "RPA_MANAGEMENT";

	// アカウント単位タスクのスレッドプール
	// セルフチェックで監視する。
	private static ThreadPoolExecutor threadPool = new MonitoredThreadPoolExecutor(3, 3, 0L,
			TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(), 
			new RPAResourceMonitorThreadFactory("RPA-AutoDetectionService-thread-%d"));
	
	// threadPoolでRPAスコープID単位の排他制御を行うためのSet
	private static Set<String> detecting = new CopyOnWriteArraySet<>();
	

	// RPAリソース自動検知用のThreadFactory
	private static class RPAResourceMonitorThreadFactory implements ThreadFactory {
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String threadName;
		
		public RPAResourceMonitorThreadFactory(String threadName) {
			this.threadName = threadName;
		}

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(new Runnable() {
				@Override
				public void run() {
					// ThreadLocalを設定
					String userId = HinemosPropertyCommon.rpa_internal_thread_admin_user.getStringValue();
					HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);
					try {
						HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
					} catch (HinemosUnknown e) {
						throw new RuntimeException(e);
					}
					r.run();
				}					
			}, String.format(threadName, threadNumber.getAndIncrement()));
		}
	}

	public void execute() throws HinemosUnknown {
		try {
			resourceUpdate();
		} catch (Exception e) {
			throw new HinemosUnknown(e);
		}
	}

	public static void resourceUpdate() throws Exception {
		m_log.debug("Start async auto-detect...");
		
		List<RpaManagementToolAccount> accounts = new RpaControllerBean().getRpaAccountList(); 
		internalResourceUpdate(accounts);
		
		m_log.debug("Finish async auto-detect.");
	}
	
	public static void resourceUpdate(RpaManagementToolAccount account) throws Exception {
		m_log.debug("Start async auto-detect...");
		try {
			internalResourceUpdate(Arrays.asList(new RpaControllerBean().getRpaAccount(account.getRpaScopeId())));
		} catch (RpaManagementToolAccountNotFound e) {
			// アカウントが見つからない場合、リソースを削除する。
			internalResourceRemove(account);
		}
		m_log.debug("Finish async auto-detect.");
	}

	private static void internalResourceUpdate(List<RpaManagementToolAccount> rpaManagementToolAccounts) throws Exception {
		if (!HinemosPropertyCommon.rpa_autoupdate_enable.getBooleanValue()) {
			// プロパティで自動検知が無効な場合は実施しない。
			return;
		}
		
		// アカウント毎にRPAリソースを更新
		for (RpaManagementToolAccount account : rpaManagementToolAccounts) {
			if (detecting.contains(account.getRpaScopeId())) {
				m_log.debug(String.format("already queued for autoupdate : %s", account.getRpaScopeId()));
				continue;
			}
			
			m_log.debug(String.format("submit for autoupdate : %s", account.getRpaScopeId()));
			detecting.add(account.getRpaScopeId());
			threadPool.submit(() -> {
				try {
					// RPA管理ツールからリソース情報を取得し、マネージャのRPAリソース情報を更新
					RepositoryUpdater updater = new RepositoryUpdater(); 
					updater.update(account);
				} catch (Exception e) {
					notifyFailedUpdate(account.getRpaScopeId());
				}  finally {
					detecting.remove(account.getRpaScopeId());
				}
			});
		}
	}
	
	// RPAリソースを削除する。
	// 個々のアカウント削除時の処理のため、スレッドプールではなく同一スレッドで実行
	public static void internalResourceRemove(RpaManagementToolAccount rpaManagementToolAccount) throws UsedFacility, InvalidRole {
		while (detecting.contains(rpaManagementToolAccount.getAccountId())) {
			// 自動検知が実行中の場合、終了するまで待つ。
			try{
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				m_log.warn(e.getMessage(), e);
				return;
			}
		}
		
		detecting.add(rpaManagementToolAccount.getAccountId());
		try {
			new RepositoryUpdater().remove(rpaManagementToolAccount);
		} catch (HinemosUnknown e) {
			notifyFailedUpdate(rpaManagementToolAccount.getRpaScopeId());
		} finally {
			detecting.remove(rpaManagementToolAccount.getAccountId());
		}
	}

	/**
	 * 自動検知
	 * 
	 * Hinemosプロパティ"rpa.autoupdate.interval"を"OFF"にすることで、無効化できる。
	 */
	@SuppressWarnings("unchecked")
	public static void start() {
		try {
			String cronString = HinemosPropertyCommon.rpa_autoupdate_interval.getStringValue();
			if(!"off".equals(cronString)) {
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, RpaResourceMonitor.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} else {
				m_log.debug("Skipping RpaResourceMonitor.");
			}
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			// 起動に失敗した場合、cron 文字列を既定で再試行。
			try {
				String cronString = HinemosPropertyCommon.rpa_autoupdate_interval.getBean().getDefaultStringValue();
				m_log.warn(String.format("invalid cron expression expression:rpa.autoupdate.interval = %s, so schedule auto-detection with default cron expression:%s", 
						HinemosPropertyCommon.rpa_autoupdate_interval.getStringValue(),
						cronString
						));
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, RpaResourceMonitor.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} catch (HinemosUnknown|NullPointerException e1) {
				m_log.warn(e.getMessage(), e);
			} catch (Exception e1) {
				m_log.warn(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * リソースの自動検知でRPA管理ツールへのアクセスに失敗したことを通知する。
	 */
	private static void notifyFailedUpdate(String rpaScopeId) {
		AplLogger.put(InternalIdCommon.RPA_SYS_004, PriorityConstant.TYPE_WARNING, new String[]{rpaScopeId} , MessageConstant.MESSAGE_RPA_AUTO_DETECT_ACCESS_FAILED.getMessage(rpaScopeId));
	}
	}
