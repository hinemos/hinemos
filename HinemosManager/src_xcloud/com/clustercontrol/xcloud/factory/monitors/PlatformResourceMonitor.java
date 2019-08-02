/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.plugin.util.scheduler.JobKey;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.ContextBean;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.ActionMode;
import com.clustercontrol.xcloud.factory.CacheResourceManagement;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.ICloudScopes;
import com.clustercontrol.xcloud.factory.IInstances;
import com.clustercontrol.xcloud.factory.IRepository;
import com.clustercontrol.xcloud.factory.IStorages;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallable;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.util.CloudMessageUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.Tuple;

public class PlatformResourceMonitor extends CloudManagerJob {
	private static final Logger logger = Logger.getLogger(PlatformResourceMonitor.class);

	private static final String jobName = "AutoDetection";
	private static final String jobGroupName = "CLOUD_MANAGEMENT";
	public static final JobKey key = JobKey.jobKey(jobName, jobGroupName);
	
	private static class QueueInfo {
		public boolean threadStart = false;
		public Map<Tuple, UpdateInfo> resourceMap = new HashMap<>();
	}

	private static final ReentrantLock lock = new ReentrantLock();

	private static final QueueInfo queue = new QueueInfo();
	
	public interface AutoDetectionListner {
		void onPreUpdateBranch();
		void onPostUpdateBranch();
		void onPreUpdateRoot();
		void onPostUpdateRoot();
	}
	
	private static ThreadPoolExecutor mainThread = new ThreadPoolExecutor(1, 1, 0L,
			TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(new Runnable() {
						@Override
						public void run() {
							try (SessionScope scope = Session.SessionScope.open()) {
								String userId = HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue();
								HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);
								Session.current().setHinemosCredential(new HinemosCredential(userId));
								
								try {
									HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
								} catch (HinemosUnknown e) {
									throw new InternalManagerError(e);
								}
								r.run();
							}
						}
					}, "AutoDetectionService-main-thread");
				}
			});
	
	private static ThreadPoolExecutor threadPool = new MonitoredThreadPoolExecutor(3, 3, 0L,
			TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
				private final AtomicInteger threadNumber = new AtomicInteger(1);
				
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(new Runnable() {
						@Override
						public void run() {
							try (SessionScope scope = Session.SessionScope.open()) {
								String userId = HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue();
								HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);
								Session.current().setHinemosCredential(new HinemosCredential(userId));
								
								try {
									HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
								} catch (HinemosUnknown e) {
									throw new InternalManagerError(e);
								}
								r.run();
							}
						}
						
					}, "AutoDetectionService-thread-" + threadNumber.getAndIncrement()) {
					};
				}
			}) {
		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			Session.poll();
		}
	};
	
	@Override
	public void internalExecute() throws Exception {
		resourceUpdate();
	}

	public static void resourceUpdate() throws Exception {
		if (lock.tryLock()) {
			try {
				ActionMode.enterAutoDetection();
				logger.debug("Start async auto-detect...");
				for (AutoDetectionListner listener: listeners) {
					listener.onPreUpdateRoot();
				}
				
				ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
				internalResourceUpdate(scopes.getAllCloudScopes());
				
				for (AutoDetectionListner listener: listeners) {
					listener.onPostUpdateRoot();
				}
				
				logger.debug("Finish async auto-detect.");
			} finally {
				ActionMode.leaveAutoDetection();
				lock.unlock();
			}
		}
	}
	
	public static void resourceUpdate(final String cloudScopeId) throws Exception {
		if (lock.tryLock()) {
			try {
				ActionMode.enterAutoDetection();
				logger.debug("Start async auto-detect...");
				
				for (AutoDetectionListner listener: listeners) {
					listener.onPreUpdateRoot();
				}
				
				CloudScopeEntity cloudScope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
				internalResourceUpdate(Arrays.asList(cloudScope));
				
				for (AutoDetectionListner listener: listeners) {
					listener.onPostUpdateRoot();
				}
				
				logger.debug("Finish async auto-detect.");
			} finally {
				ActionMode.leaveAutoDetection();
				lock.unlock();
			}
		}
	}
	
	private static class UpdateInfo {
		public CloudScopeEntity cloudScope;
		public LocationEntity location;
		public Future<CacheResourceManagement> future;
	}
	
	private static void internalResourceUpdate(List<CloudScopeEntity> cloudScopes) throws Exception {
		synchronized (queue) {
			final ContextBean contextData = Session.current().getContext();
			
			for (CloudScopeEntity cloudScope: cloudScopes) {
				for (LocationEntity location: cloudScope.getLocations()) {
					Tuple key = Tuple.build(cloudScope.getId(), location.getLocationId());
					
					if (queue.resourceMap.containsKey(key)) {
						logger.debug(String.format("already queued for autoupdate : %s", key));
						continue;
					}
					
					logger.debug(String.format("submit for autoupdate : %s", key));

					Future<CacheResourceManagement> future = threadPool.submit(new Callable<CacheResourceManagement>() {
						@Override
						public CacheResourceManagement call() throws Exception {
							try (SessionScope sessionScope = SessionScope.open(contextData)) {
								long start = System.currentTimeMillis();
								logger.debug(String.format("Start getting cloud resources. %s", key));

								CacheResourceManagement holder = location.getCloudScope().optionCall(new OptionCallable<CacheResourceManagement>() {
									@Override
									public CacheResourceManagement call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
										CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScope.getId());
										
										CacheResourceManagement holder = CacheResourceManagement.create(location, user);
										holder.selectResourceHierarchy();

										if(HinemosPropertyCommon.xcloud_autoupdate_storage_enable.getBooleanValue())
											holder.selectStorage();
											if(HinemosPropertyCommon.xcloud_autoupdate_storage_snapshot_enable.getBooleanValue())
												holder.selectStorageSnapshot();
										if(HinemosPropertyCommon.xcloud_autoupdate_instance_snapshot_enable.getBooleanValue())
											holder.selectInstanceSnapshots();

										return holder;
									}
								});

								long elasped = System.currentTimeMillis() - start;
								String msg = String.format("end getting cloud resources. %s elapsed=%d ms", key, elasped);
								if(elasped > 60_000){
									logger.info(msg);
								}else{
									logger.debug(msg);
								}

								return holder;
							} catch(CloudManagerException e) {
								CloudMessageUtil.notify_AutoUpadate_Error(cloudScope.getId(), location.getLocationId(), e);
								if (!ErrorCode.UNEXPECTED.match(e) && !ErrorCode.HINEMOS_MANAGER_ERROR.match(e) && e.getCause() == null) {
									logger.warn(HinemosMessage.replace(e.getMessage()));
								} else {
									logger.warn(HinemosMessage.replace(e.getMessage()), e);
								}
							} catch (Exception e) {
								CloudMessageUtil.notify_AutoUpadate_Error(cloudScope.getId(), location.getLocationId(), e);
								logger.warn(e.getMessage(), e);
							}
							return null;
						}
					});
					
					UpdateInfo info = new UpdateInfo();
					info.cloudScope = cloudScope;
					info.location = location;
					info.future = future;
					
					queue.resourceMap.put(Tuple.build(cloudScope.getId(), location.getLocationId()), info);
				}
			}
			
			if (!queue.threadStart) {
				mainThread.execute(new Runnable() {
					@Override
					public void run() {
						try (SessionScope sessionScope = SessionScope.open(contextData)) {
							IRepository repository = CloudManager.singleton().getRepository();
							while(true) {
								List<Map.Entry<Tuple, UpdateInfo>> list;
								synchronized(queue) {
									list = new ArrayList<>(queue.resourceMap.entrySet());
								}
								
								for (Map.Entry<Tuple, UpdateInfo> entry: list) {
									if (entry.getValue().future.isDone()) {
										try {
											logger.debug(String.format("start autoupdate. cloudscope=%s,location=%s", entry.getValue().cloudScope.getId(), entry.getValue().location.getLocationId()));
											
											long start_total = System.currentTimeMillis();
											
											for (AutoDetectionListner listener: listeners) {
												listener.onPreUpdateBranch();
											}
											
											CacheResourceManagement crm = entry.getValue().future.get();
											if (crm != null) {
												repository.setCacheResourceManagement(crm);
												
												{
													long start = System.currentTimeMillis();
													logger.debug(String.format("start updating location repository. %s", entry.getKey()));
													repository.updateLocationRepository(entry.getValue().cloudScope.getCloudScopeId(), entry.getValue().location.getLocationId());
													logger.debug(String.format("end updating location repository. %s elapsed=%d", entry.getKey(), System.currentTimeMillis() - start));
												}
												

												if(HinemosPropertyCommon.xcloud_autoupdate_instance_snapshot_enable.getBooleanValue()){
													long start = System.currentTimeMillis();
													logger.debug(String.format("start updating instance snapshots. %s", entry.getKey()));
													try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
														IInstances instances = CloudManager.singleton().getInstances(entry.getValue().cloudScope.getAccount(), entry.getValue().location);
														instances.setCacheResourceManagement(crm).updateInstanceBackups(CloudUtil.emptyList(String.class));
														scope.complete();
													}
													logger.debug(String.format("end updating instance snapshots. %s elapsed=%d", entry.getKey(), System.currentTimeMillis() - start));
												}

												if(HinemosPropertyCommon.xcloud_autoupdate_storage_enable.getBooleanValue()) {
													try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
														IStorages storages = CloudManager.singleton().getStorages(entry.getValue().cloudScope.getAccount(), entry.getValue().location);
														{
															long start = System.currentTimeMillis();
															logger.debug(String.format("start updating storages. %s", entry.getKey()));
															storages.setCacheResourceManagement(crm).updateStorages(CloudUtil.emptyList(String.class));
															logger.debug(String.format("end updating storages. %s elapsed=%d", entry.getKey(), System.currentTimeMillis() - start));
														}
														if(HinemosPropertyCommon.xcloud_autoupdate_storage_snapshot_enable.getBooleanValue()){
															long start = System.currentTimeMillis();
															logger.debug(String.format("start updating storage snapshots. %s", entry.getKey()));
															storages.updateStorageBackups(CloudUtil.emptyList(String.class));
															logger.debug(String.format("end updating storage snapshots. %s elapsed=%d", entry.getKey(), System.currentTimeMillis() - start));
														}
														scope.complete();
													}
												}
												
												threadPool.execute(new Runnable() {
													@Override
													public void run() {
														try {
															logger.debug(String.format("update cloud resources. %s", entry.getKey()));
															crm.update();
														} catch(Exception e) {
															CloudMessageUtil.notify_AutoUpadate_Error(entry.getValue().cloudScope.getId(), entry.getValue().location.getLocationId(), e);
														}
													}
												});
											} else {
												logger.warn(String.format("fail to get CacheResourceManagement. cloudscope=%s,location=%s", entry.getValue().cloudScope.getId(), entry.getValue().location.getLocationId()));
											}
											
											for (AutoDetectionListner listener: listeners) {
												listener.onPostUpdateBranch();
											}
											logger.debug(String.format("autoupdate is complete. %s elapsed=%d", entry.getKey(), System.currentTimeMillis() - start_total));
										} catch(CloudManagerException e) {
											CloudMessageUtil.notify_AutoUpadate_Error(entry.getValue().cloudScope.getId(), entry.getValue().location.getLocationId(), e);
											if (!ErrorCode.UNEXPECTED.match(e) && !ErrorCode.HINEMOS_MANAGER_ERROR.match(e) && e.getCause() == null) {
												logger.warn(HinemosMessage.replace(e.getMessage()));
											} else {
												logger.warn(HinemosMessage.replace(e.getMessage()), e);
											}
										} catch(InterruptedException | ExecutionException e) {
											CloudMessageUtil.notify_AutoUpadate_Error(entry.getValue().cloudScope.getId(), entry.getValue().location.getLocationId(), e);
											logger.warn(e.getMessage(), e);
										} catch(Exception e) {
											CloudMessageUtil.notify_AutoUpadate_Error(entry.getValue().cloudScope.getId(), entry.getValue().location.getLocationId(), e);
											logger.warn(e.getMessage(), e);
										} finally {
											synchronized(queue) {
												logger.debug(String.format("remove from autoupdate queue. %s", entry.getKey()));
												queue.resourceMap.remove(entry.getKey());
											}
										}
									} else if (entry.getValue().future.isCancelled()) {
										synchronized(queue) {
											logger.debug(String.format("autoupdate is cancel. %s", entry.getKey()));
											queue.resourceMap.remove(entry.getKey());
										}
									}
								}
								
								boolean loop;
								synchronized(queue) {
									if (queue.resourceMap.isEmpty()) {
										queue.threadStart = false;
									}
									loop = queue.threadStart;
								}
								
								if (!loop) {
									logger.debug("main thread for autoupdate stop");
									break;
								}
								
								try {
									Thread.sleep(1000);
								} catch (Exception e) {
								}
							}
						}
					}
				});
				queue.threadStart = true;
			}else{
				logger.info("Thread already started");
			}
		}
	}

	private static List<AutoDetectionListner> listeners = Collections.synchronizedList(new ArrayList<AutoDetectionListner>());

	/**
	 * 自動検知
	 * 
	 * CloudPropertyConstants.autoupdate_interval(hinemos.cloud.autoupdate.interval)を"OFF"にすることで、無効化(Skip)できる。
	 */
	@SuppressWarnings("unchecked")
	public static void start() {
		try {
			String cronString = HinemosPropertyCommon.xcloud_autoupdate_interval.getStringValue();
			if(!"off".equals(cronString)) {
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, PlatformResourceMonitor.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} else {
				logger.debug("Skipping PlatformResourceMonitor.");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			
			// 起動に失敗した場合、cron 文字列を既定で再試行。
			try {
				String cronString = HinemosPropertyCommon.xcloud_autoupdate_interval.getStringValue();
				SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, PlatformResourceMonitor.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			} catch (HinemosUnknown|NullPointerException e1) {
				logger.warn(e.getMessage(), e);
			} catch (Exception e1) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	public static void stop() {
		try {
			SchedulerPlugin.deleteJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName);
		} catch (HinemosUnknown e) {
			Logger.getLogger(PlatformResourceMonitor.class).warn(e.getMessage(), e);
		}
	}

	public static void addListener(AutoDetectionListner listner) {
		listeners.add(listner);
	}

	public static void removeListener(AutoDetectionListner listner) {
		listeners.remove(listner);
	}
}
