/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudLoginUser;
import com.clustercontrol.ws.xcloud.HRepository;
import com.clustercontrol.ws.xcloud.InstanceBackup;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.repository.CloudRepository;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class HinemosManager extends Element implements IHinemosManager {
	
	private static final Log logger = LogFactory.getLog(HinemosManager.class);
	
	private String managerName;
	private String url;
	
	private boolean Initialized;

	private EndpointManager endpointManager;
	
	private ElementBaseModeWatch modelWatcher;
	
	private List<CloudPlatform> cloudPlatforms;

	private CloudScopes cloudScopes;
	private CloudRepository cloudRepository;
	private BillingMonitors billingAlarms;
	
	public HinemosManager(String managerName, String url){
		this.url = url;
		this.managerName = managerName;
		this.endpointManager = new EndpointManager(managerName);
		this.cloudRepository = new CloudRepository(this);
		this.billingAlarms = new BillingMonitors(this);
	}

	public <T> T getEndpoint(Class<T> clazz) {
		return endpointManager.getEndpoint(clazz);
	}

	@Override
	public String getAccountName() {
		return endpointManager.getAccountName();
	}

	@Override
	public String getManagerName(){
		return managerName;
	}

	@Override
	public CloudRepository getCloudRepository() {
		if(cloudRepository == null){
			cloudRepository = new CloudRepository(this);
		}
		return cloudRepository;
	}

	@Override
	public CloudPlatform[] getCloudPlatforms() {
		if (cloudPlatforms == null)
			update();
		return cloudPlatforms.toArray(new CloudPlatform[cloudPlatforms.size()]);
	}

	@Override
	public ICloudPlatform getCloudPlatform(String cloudPlatformId) {
		for(CloudPlatform cloudPlatform: getCloudPlatforms()){
			if(cloudPlatform.getId().equals(cloudPlatformId)){
				return cloudPlatform;
			}
		}
		throw new CloudModelException(String.format("Not found platform of %s", cloudPlatformId));
	}

	@Override
	public void update() {
		long start1 = System.currentTimeMillis();
		
		try {
			CloudEndpoint endpoint = getEndpoint(CloudEndpoint.class);
			
			long start2 = System.currentTimeMillis();
			HRepository repository = endpoint.getRepository();
			logger.debug(String.format("CloudEndpoint.getRepository : elapsed time=%dms", System.currentTimeMillis() - start2));

			updateCloudPlatforms(repository.getPlatforms());

			updateCloudScopes(repository);
			
			updateCloudRepository(repository);
			
			{
				Map<String, List<CloudLoginUser>> userMap = new HashMap<>();
				for (CloudLoginUser user: repository.getLoginUsers()) {
					List<CloudLoginUser> users = userMap.get(user.getCloudScopeId());
					if (users == null) {
						users = new ArrayList<>();
						userMap.put(user.getCloudScopeId(), users);
					}
					users.add(user);
				}
				for (CloudScope scope: getCloudScopes().getCloudScopes()) {
					List<CloudLoginUser> users = userMap.get(scope.getId());
					if (users != null)
						scope.getLoginUsers().update(users);
				}
			}
			
			logger.debug(String.format("HinemosManager.update : elapsed time=%dms", System.currentTimeMillis() - start1));
			
			Initialized = true;
		} catch (Exception e) {
			throw new CloudModelException(e.getMessage(), e);
		}
	}
	
	public void updateCloudPlatforms(List<com.clustercontrol.ws.xcloud.CloudPlatform> wsCloudPlatforms) {
		if (cloudPlatforms == null)
			cloudPlatforms = new ArrayList<>();

		CollectionComparator.compareCollection(cloudPlatforms, wsCloudPlatforms, new CollectionComparator.Comparator<CloudPlatform, com.clustercontrol.ws.xcloud.CloudPlatform>() {
			public boolean match(CloudPlatform o1, com.clustercontrol.ws.xcloud.CloudPlatform o2) {return o1.equalValues(o2);}
			public void matched(CloudPlatform o1, com.clustercontrol.ws.xcloud.CloudPlatform o2) {o1.update(o2);}
			public void afterO1(CloudPlatform o1) {cloudPlatforms.remove(o1);}
			public void afterO2(com.clustercontrol.ws.xcloud.CloudPlatform o2) {
				CloudPlatform newCloudPlatform = CloudPlatform.convert(HinemosManager.this, o2);
				cloudPlatforms.add(newCloudPlatform);
			}
		});
	}

	@Override
	public void updateLocation(final ILocation location) {
		CloudEndpoint endpoint = getEndpoint(CloudEndpoint.class);
		
		HRepository repository;
		try {
			repository = endpoint.updateLocationRepository(location.getCloudScope().getId(), location.getId());
		} catch (Exception e) {
			throw new CloudModelException(e.getMessage(), e);
		}
		
		ComputeResources resources = getCloudScopes().getCloudScope(location.getCloudScope().getId()).getLocation(location.getId()).getComputeResources();

		List<com.clustercontrol.ws.xcloud.Instance> instances = new ArrayList<>(repository.getInstances());
		Iterator<com.clustercontrol.ws.xcloud.Instance> iter = instances.iterator();
		while (iter.hasNext()) {
			com.clustercontrol.ws.xcloud.Instance instance = iter.next();
			if (!(instance.getCloudScopeId().equals(location.getCloudScope().getId()) && instance.getLocationId().equals(location.getId()))) {
				iter.remove();
			}
		}
		resources.updateInstances(instances);
		
		Map<String, InstanceBackup> instanceBackupMap = new HashMap<>();
		for (InstanceBackup backup: repository.getInstanceBackups()) {
			instanceBackupMap.put(backup.getInstanceId(), backup);
		}
		for (Instance i: resources.getInstances()) {
			i.getBackup().update(instanceBackupMap.get(i.getId()));
		}
		
		List<com.clustercontrol.ws.xcloud.Storage> storages = new ArrayList<>(repository.getStorages());
		Iterator<com.clustercontrol.ws.xcloud.Storage> storageIter = storages.iterator();
		while (storageIter.hasNext()) {
			com.clustercontrol.ws.xcloud.Storage storage = storageIter.next();
			if (!(storage.getCloudScopeId().equals(location.getCloudScope().getId()) && storage.getLocationId().equals(location.getId()))) {
				iter.remove();
			}
		}
		resources.updateStorages(storages);
		
		Map<String, com.clustercontrol.ws.xcloud.StorageBackup> storageBackupMap = new HashMap<>();
		for (com.clustercontrol.ws.xcloud.StorageBackup backup: repository.getStorageBackups()) {
			storageBackupMap.put(backup.getStorageId(), backup);
		}
		for (Storage s: resources.getStorages()) {
			s.getBackup().update(storageBackupMap.get(s.getId()));
		}

		getCloudRepository().updateLocation(location, repository);
	}
	
	public void updateCloudScopes(HRepository repository) {
		getCloudScopes().updateCloudScopes(repository);
	}
	
	public void updateLoginUsers() {
		for (CloudScope scope: getCloudScopes().getCloudScopes()) {
			scope.getLoginUsers().update();
		}
	}
	
	public void updateCloudRepository(HRepository repository) {
		getCloudRepository().update(repository);
	}
	
	@Override
	public CloudScopes getCloudScopes() {
		if (cloudScopes == null)
			cloudScopes = new CloudScopes(this);
		return cloudScopes;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public ElementBaseModeWatch getModelWatch() {
		if (modelWatcher == null)
			modelWatcher = new ElementBaseModeWatch(this);
		return modelWatcher;
	}

	@Override
	public String toString() {
		return "HinemosManager [managerName=" + managerName + ", url=" + url
				+ "]";
	}

	@Override
	public IBillingMonitors getBillingAlarms() {
		return billingAlarms;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IHinemosManager.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public boolean isInitialized() {
		return Initialized;
	}
}
