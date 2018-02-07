/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.bean.InstanceStatus;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.IInstances;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;


public class InstanceMonitorService {
	public interface InstanceTraceListner {
		void onNotify(String cloudScopeId, InstanceEntity instance);
		void onError(String cloudScopeId, String instanceId, Exception e);
	}

	private static class InstanceInfo {
		public Session.ContextBean data;
		public String cloudScopeId;
		public String locationId;
		public InstanceStatus[] stoppedStatus;
		public int pingCount;
	}

	private Map<String, InstanceInfo> instanceInfoMap = new HashMap<String, InstanceInfo>();
	private final int interval;
	private final int maxPingCount;
	private final ScheduledExecutorService executor;

	private List<InstanceTraceListner> listeners = Collections.synchronizedList(new ArrayList<InstanceTraceListner>());

	/**
	 * instanceMap に合わせて同期される。
	 */
	private ScheduledFuture<?> sf;
	
	private InstanceMonitorService(int interval, int maxPingCount) {
		this.interval = interval;
		this.maxPingCount = maxPingCount;
		this.executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
				@Override
				public Thread newThread(final Runnable r) {
					return new Thread(r, "InstanceMonitorService-thread-1");
				}
			}) {

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				Session.poll();
			}
		};
	}
	
	/**
	 * 追跡開始。
	 * 
	 * @param indtanecId
	 * @param listener
	 */
	public void startMonitor(String cloudScopeId, String locationId, String indtanecId, Session.ContextBean data, InstanceStatus...stoppedStatus) {
		if (!HinemosPropertyCommon.xcloud_autoupdate_node.getBooleanValue()) {
			return;
		}
		
		synchronized (instanceInfoMap) {
			InstanceInfo info = new InstanceInfo();
			info.data = data;
			info.cloudScopeId = cloudScopeId;
			info.locationId = locationId;
			info.stoppedStatus = stoppedStatus;
			info.pingCount = 0;
			
			// 問題が発生する可能性がある。
			instanceInfoMap.put(indtanecId, info);
			
			if (sf == null) {
				sf = executor.scheduleWithFixedDelay(
					new Runnable() {
						@Override
						public void run() {
							synchronized(instanceInfoMap) {
								Logger logger = Logger.getLogger(InstanceMonitorService.class);
								
								// 既存のインスタンスの情報が取得できている場合、更新。
								for (final Map.Entry<String, InstanceInfo> entry: instanceInfoMap.entrySet()) {
									logger.debug("instanceid : " + entry.getKey() + ", condition : " + Arrays.toString(entry.getValue().stoppedStatus));
									
									++entry.getValue().pingCount;
									
									try (SessionScope sessionScope = SessionScope.open(entry.getValue().data)) {
										CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(entry.getValue().cloudScopeId);
										IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(entry.getValue().locationId));
										
										List<InstanceEntity> instanceEntities = instances.updateInstances(Arrays.asList(entry.getKey()));
										if (!instanceEntities.isEmpty()) {
											InstanceEntity instance = instanceEntities.get(0);
											boolean finished = false;
											for (InstanceStatus status: entry.getValue().stoppedStatus) {
												if (status == instance.getInstanceStatus()) {
													finished = true;
													break;
												}
											}
											
											if (finished) {
												instanceInfoMap.remove(entry.getKey());
												for (InstanceTraceListner listener: listeners) {
													listener.onNotify(entry.getValue().cloudScopeId, instance);
												}
											} else {
												if (maxPingCount == entry.getValue().pingCount) {
													instanceInfoMap.remove(entry.getKey());
												}
											}
										} else {
											instanceInfoMap.remove(entry.getKey());
										}
									} catch (CloudManagerException e) {
										logger.error(e.getMessage(), e);
										instanceInfoMap.remove(entry.getKey());
										
										for (InstanceTraceListner listener: listeners) {
											listener.onError(entry.getValue().cloudScopeId, entry.getKey(), e);
										}
									} catch (RuntimeException e) {
										logger.error(e.getMessage(), e);
										instanceInfoMap.remove(entry.getKey());
										
										for (InstanceTraceListner listener: listeners) {
											listener.onError(entry.getValue().cloudScopeId, entry.getKey(), e);
										}
									}
								}
								
								mustStop();
							}
						}
					},
					interval,
					interval,
					TimeUnit.MILLISECONDS); 
			}
		}
	}
	
	/**
	 * 追跡停止。
	 * 
	 * @param indtanecId
	 */
	public void stopMonitor(String indtanecId) {
		synchronized (instanceInfoMap) {
			instanceInfoMap.remove(indtanecId);
			mustStop();
		}
	}
	
	private void mustStop() {
		if (sf != null && instanceInfoMap.isEmpty()) {
			sf.cancel(true);
			sf = null;
		}
	}
	
	public void shutdown() {
		executor.shutdown();
		instanceInfoMap.clear();
	}
	
	public void addListener(InstanceTraceListner listner) {
		listeners.add(listner);
	}
	public void removeListener(InstanceTraceListner listner) {
		listeners.remove(listner);
	}

	private static volatile InstanceMonitorService singleton;
	
	public static InstanceMonitorService getSingleton() {
		if (singleton == null) {
			synchronized (InstanceMonitorService.class) {
				if (singleton == null) {
					singleton = new InstanceMonitorService(
							HinemosPropertyCommon.xcloud_registcheck_interval.getIntegerValue(),
							HinemosPropertyCommon.xcloud_registcheck_count.getIntegerValue());
				}
			}
		}
		return singleton;
	}
}
