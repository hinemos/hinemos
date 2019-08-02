/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.ICloudScopes;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionExecutor;

public class BillingDetailMonitor {
	public static final String jobGroupName = "CLOUD_MANAGEMENT";
	public static final String collectJobName = "collectBilling";


	/**
	 * 課金詳細の自動更新
	 * 
	 * CloudPropertyConstants.billing_detail_collect_time(hinemos.cloud.billing.detail.collect.time)を"OFF"にすることで、無効化(Skip)できる。
	 */
	@SuppressWarnings("unchecked")
	public static void start() {
		try {
			String cronString = HinemosPropertyCommon.xcloud_billing_detail_collect_time.getStringValue();
			if(! "off".equals(cronString)) {
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, collectJobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, CollectBillingJob.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} else {
				Logger.getLogger(BillingDetailMonitor.class).debug("Skipping BillingDetailMonitor.");
			}
		} catch (Exception e) {
			Logger.getLogger(BillingDetailMonitor.class).warn(e.getMessage(), e);

			// 起動に失敗した場合、cron 文字列を既定で再試行。
			try {
				String cronString = HinemosPropertyCommon.xcloud_billing_detail_collect_time.getStringValue();
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, collectJobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, CollectBillingJob.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} catch (HinemosUnknown|NullPointerException e1) {
				Logger.getLogger(BillingDetailMonitor.class).warn(e.getMessage(), e);
			} catch (Exception e1) {
				Logger.getLogger(BillingDetailMonitor.class).warn(e.getMessage(), e);
			}
		}
	}
	
	public static void stop() {
		try {
			SchedulerPlugin.deleteJob(SchedulerType.RAM_MONITOR, collectJobName, jobGroupName);
		} catch (HinemosUnknown e) {
			Logger.getLogger(BillingDetailMonitor.class).warn(e.getMessage(), e);
		}
	}

	// 課金情報収集ジョブ
	public static class CollectBillingJob extends CloudManagerJob {
		private static ExecutorService threadPool = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
					private final AtomicInteger threadNumber = new AtomicInteger(1);
					@Override
					public Thread newThread(final Runnable r) {
						return new Thread(new Runnable() {
								@Override
								public void run() {
									try (SessionScope scope = Session.SessionScope.open()) {
										HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue());
										HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);
										Session.current().setHinemosCredential(new HinemosCredential(HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue()));
										r.run();
									}
								}
							}, "collect_billing-thread-" + threadNumber.getAndIncrement());
						}
					}
				);

		private static final Set<String> set = new HashSet<String>();

		public CollectBillingJob() {
		}

		@Override
		protected void internalExecute() throws Exception {
			ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
			for (CloudScopeEntity scope: scopes.getCloudScopesByCurrentHinemosUser()) {
				if ((scope.getBillingDetailCollectorFlg() != null && !scope.getBillingDetailCollectorFlg())) {
					continue;
				}
				
				scope.optionExecute(new OptionExecutor() {
					@Override
					public void execute(final CloudScopeEntity scope, final ICloudOption option) throws CloudManagerException {
						if (!option.getCloudSpec().isBillingAlarmEnabled())
							return;
						
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								if (!acquire(scope.getId())) return;

								try {
									// 課金情報を更新
									option.getBillingManagement(scope).updateBillingDetail();
								} catch (CloudManagerException e) {
									Logger.getLogger(this.getClass()).error(e.toString(), e);
								} finally {
									release(scope.getId());
								}
							}
						});
					}
				});
			}
		}
		
		public static void collectBillingDetails(String cloudScopeId) throws Exception {
			CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);

			if ((scope.getBillingDetailCollectorFlg() != null && !scope.getBillingDetailCollectorFlg()))
				return;
			
			scope.optionExecute(new OptionExecutor() {
				@Override
				public void execute(final CloudScopeEntity scope, final ICloudOption option) throws CloudManagerException {
					if (!option.getCloudSpec().isBillingAlarmEnabled())
						return;

					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							if (!acquire(scope.getId())) return;

							try {
								// 課金情報を更新
								option.getBillingManagement(scope).updateBillingDetail();
							} catch (CloudManagerException e) {
								Logger.getLogger(this.getClass()).error(e.toString(), e);
							} finally {
								release(scope.getId());
							}
						}
					});
				}
			});
		}
		private static synchronized boolean acquire(String cloudScopeId) {
			if (!set.contains(cloudScopeId)) {
				set.add(cloudScopeId);
				return true;
			} else {
				return false;
			}
		}
		private static synchronized void release(String cloudScopeId) {
			set.remove(cloudScopeId);
		}
	}
}
