/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudPlatform;
import com.clustercontrol.ws.xcloud.HRepository;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.HinemosManager;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.platform.PlatformDependent;

/**
 * AWS へポーリングし、AWS のインスタンスを追跡する。
 *
 */
public class InstanceMonitorService {
	
	private static final Log logger = LogFactory.getLog(InstanceMonitorService.class);
	
	private static class InstanceIdentity {
		public InstanceIdentity(String cloudScopeId, String locationId, String instanceId, String[] stateTypes) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.instanceId = instanceId;
			this.stateTypes = stateTypes;
		}
		public String cloudScopeId;
		public String locationId;
		public String instanceId;
		public String[] stateTypes;
	}
	
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1) {
		@Override
	    protected void afterExecute(Runnable r, Throwable t) {
	    	if (t != null) {
	    		logger.debug(t.getMessage(), t);
	    	}
	    }
	};

	/**
	 * 同時する必要あり。
	 */
	private Map<String, Map<String, InstanceIdentity>> stoppedStatusMap = Collections.synchronizedMap(new HashMap<String,  Map<String, InstanceIdentity>>());
	private Map<String, CloudEndpoint> endpointMap = Collections.synchronizedMap(new HashMap<String, CloudEndpoint>());
	
	/**
	 * instanceMap に合わせて同期される。
	 */
	private ScheduledFuture<?> sf;
	
	private Display display;
	
    private static final ThreadLocal<InstanceMonitorService> instanceMonitorService = 
        new ThreadLocal<InstanceMonitorService>() {
            @Override
            protected InstanceMonitorService initialValue() {
                return new InstanceMonitorService();
            }
    	};
	
	private InstanceMonitorService() {
		display = Display.getCurrent();
	}
	
	/**
	 * 追跡開始。
	 * 
	 * @param indtanecId
	 * @param listener
	 */
	public void startMonitor(String managerName, String cloudScopeId, String locationId, String instanceId, String...stoppedStatus) {
		// RAP 上では、UI スレッドのみソケットアクセスが可能なため、マルチスレッドでソケットアクセスを行う以下の以降の処理は行わない。
		if (PlatformDependent.getPlatformDependent().isRapPlatfome())
			return;
		
		synchronized (stoppedStatusMap) {
			Map<String, InstanceIdentity> identities = stoppedStatusMap.get(managerName);
			if (identities == null) {
				identities = Collections.synchronizedMap(new HashMap<String, InstanceIdentity>());
				stoppedStatusMap.put(managerName, identities);
				
				IHinemosManager manager = ClusterControlPlugin.getDefault().getHinemosManager(managerName);
				CloudEndpoint endpoint = manager.getEndpoint(CloudEndpoint.class);
				endpointMap.put(managerName, endpoint);
			}
			identities.put(instanceId, new InstanceIdentity(cloudScopeId, locationId, instanceId, stoppedStatus));
			
			if (sf == null) {
				sf = scheduleAtFixedRate(
					new Runnable() {
						@Override
						public void run() {
							try {
								// 既存のインスタンスの情報が取得できている場合、更新。
								for (final String managerName: stoppedStatusMap.keySet()) {
									CloudEndpoint endpoint = endpointMap.get(managerName);
									final List<CloudPlatform> cloudPlatforms = endpoint.getAllCloudPlatforms();
									final HRepository repository = endpoint.getRepository();
									
									display.asyncExec(new Runnable() {
										@Override
										public void run() {
											
											HinemosManager manager = (HinemosManager)ClusterControlPlugin.getDefault().getHinemosManager(managerName);
											if (manager == null)
												return;
											
											manager.updateCloudPlatforms(cloudPlatforms);
											manager.updateCloudRepository(repository);
											manager.updateCloudScopes(repository);
											
											Map<String, InstanceIdentity> identities = stoppedStatusMap.get(managerName);
											if (identities == null)
												return;
											
											List<InstanceIdentity> identityList = new ArrayList<>(identities.values());
											for (InstanceIdentity identity: identityList) {
												try {
													logger.debug(
														String.format("instance state monitoring : managerName : %s, cloudScopeId : %s, locationId : %s, instanceId : %s, expected conditions : %s",
																managerName,
																identity.cloudScopeId,
																identity.locationId,
																identity.instanceId,
																Arrays.asList(identity.stateTypes)));
														
													ICloudScope scope = manager.getCloudScopes().getCloudScope(identity.cloudScopeId);
													ILocation location = scope.getLocation(identity.locationId);
													
													IInstance instance = null;
													try {
														instance = location.getComputeResources().getInstance(identity.instanceId);
													} catch(CloudModelException e) {
													}
													
													if (instance == null) {
														identities.remove(identity.instanceId);
														continue;
													}
													
													logger.debug(
															String.format("instance state monitoring : managerName : %s, cloudScopeId : %s, locationId : %s, instanceId : %s, current conditions : %s",
																	managerName,
																	identity.cloudScopeId,
																	identity.locationId,
																	identity.instanceId,
																	instance.getStatus()));

													boolean matched = false;
													for (String state: identity.stateTypes) {
														if (state.equals(instance.getStatus())) {
															matched = true;
															break;
														}
													}
													if (matched)
														identities.remove(identity.instanceId);
												} catch(Exception e) {
													logger.warn(e.getMessage(), e);
													identities.remove(identity.instanceId);
												}
											}
											
											if (identities.isEmpty())
												stoppedStatusMap.remove(managerName);
										}
									});
								}
								mustStop();
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
					},
					10,
					10,
					TimeUnit.SECONDS); 
			}
		}
	}
	
	/**
	 * 追跡停止。
	 * 
	 * @param indtanecId
	 */
	public void stopMonitor(String indtanecId) {
		synchronized (stoppedStatusMap) {
			stoppedStatusMap.remove(indtanecId);
			mustStop();
		}
	}
	
	private void mustStop() {
		synchronized (stoppedStatusMap) {
			if (sf != null && stoppedStatusMap.isEmpty()) {
				sf.cancel(true);
				sf = null;
			}
		}
	}

	private ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	
	public void shutdown() {
		executor.shutdown();
		stoppedStatusMap.clear();
	}
	
	public static InstanceMonitorService getInstanceMonitorService() {
		return instanceMonitorService.get();
	}
}
