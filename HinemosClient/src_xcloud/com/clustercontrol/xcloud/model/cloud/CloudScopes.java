/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.ws.xcloud.HCloudScopeScope;
import com.clustercontrol.ws.xcloud.HInstanceNode;
import com.clustercontrol.ws.xcloud.HLocationScope;
import com.clustercontrol.ws.xcloud.HRepository;
import com.clustercontrol.ws.xcloud.InstanceBackup;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.HRepositoryParser;

public class CloudScopes extends Element implements ICloudScopes {
	
	private static final Log logger = LogFactory.getLog(CloudScopes.class);
	
	protected List<CloudScope> cloudScopes = new ArrayList<>();
	
	public CloudScopes(HinemosManager hinemosManager) {
		setOwner(hinemosManager);
	}

	@Override
	public CloudScope[] getCloudScopes() {
		return cloudScopes.toArray(new CloudScope[cloudScopes.size()]);
	}

	@Override
	public CloudScope getCloudScope(String CloudScopeId) {
		for (CloudScope cloudScope: getCloudScopes()) {
			if (cloudScope.getId().equals(CloudScopeId)) {
				return cloudScope;
			}
		}
		throw new CloudModelException(String.format("Not found cloudScope of %s", CloudScopeId));
	}

	@Override
	public HinemosManager getHinemosManager() {
		return (HinemosManager)getOwner();
	}
	
	public void updateCloudScopes(HRepository repository) {
		if (cloudScopes == null)
			cloudScopes = new ArrayList<>();

		final List<com.clustercontrol.ws.xcloud.CloudScope> webCloudScopes = new ArrayList<>();
		final Map<List<String>, Set<com.clustercontrol.ws.xcloud.Instance>> webInstanceMap = new HashMap<List<String>, Set<com.clustercontrol.ws.xcloud.Instance>>();
		HRepositoryParser.parse(repository, new HRepositoryParser.Handler() {
			private com.clustercontrol.ws.xcloud.CloudScope currentScope;
			private com.clustercontrol.ws.xcloud.Location currentLocation;
			public boolean cloudScopeScope(HCloudScopeScope s) {
				currentScope = (com.clustercontrol.ws.xcloud.CloudScope)s.getCloudScope();
				currentLocation = s.getLocation();
				webCloudScopes.add(currentScope);
				return true;
			};
			public boolean locationScope(HLocationScope s) {
				currentLocation = s.getLocation();
				return true;
			};
			public void instanceNode(HInstanceNode n){
				com.clustercontrol.ws.xcloud.Instance i = (com.clustercontrol.ws.xcloud.Instance)n.getInstance();
				if (!currentScope.getId().equals(i.getCloudScopeId()) || !currentLocation.getId().equals(i.getLocationId())) {
					logger.warn(String.format("instance is placed in another cloudscope tree. cloudscopeId=%s, locationId=%s, instanceId=%s",
							i.getCloudScopeId(), i.getLocationId(), i.getId()));
				} else {
					List<String> key = Arrays.asList(i.getCloudScopeId(), i.getLocationId());
					Set<com.clustercontrol.ws.xcloud.Instance> instances = webInstanceMap.get(key);
					if (instances == null) {
						instances = new HashSet<>();
						webInstanceMap.put(key, instances);
					}
					instances.add(i);
				}
			};
		});
		
		CollectionComparator.compareCollection(cloudScopes, webCloudScopes, new CollectionComparator.Comparator<CloudScope, com.clustercontrol.ws.xcloud.CloudScope>() {
			public boolean match(CloudScope o1, com.clustercontrol.ws.xcloud.CloudScope o2) {return o1.equalValues(o2);}
			public void matched(CloudScope o1, com.clustercontrol.ws.xcloud.CloudScope o2) {o1.update(o2);}
			public void afterO1(CloudScope o1) {
				internalRemoveProperty(p.cloudScopes, o1, cloudScopes);
			}
			public void afterO2(com.clustercontrol.ws.xcloud.CloudScope o2) {
				CloudScope newCloudScope = CloudScope.convert(CloudScopes.this, o2);
				internalAddProperty(p.cloudScopes, newCloudScope, cloudScopes);
			}
		});
		
		Map<List<String>, InstanceBackup> instanceBackupMap = new HashMap<>();
		for (InstanceBackup backup: repository.getInstanceBackups()) {
			instanceBackupMap.put(Arrays.asList(backup.getCloudScopeId(), backup.getInstanceId()), backup);
		}

		List<com.clustercontrol.ws.xcloud.Instance> instances = new ArrayList<>(repository.getInstances());
		for (CloudScope scope: getCloudScopes()) {
			for (Location location: scope.getLocations()) {
				List<com.clustercontrol.ws.xcloud.Instance> locationInstances = new ArrayList<>();
				Iterator<com.clustercontrol.ws.xcloud.Instance> iter = instances.iterator();
				while (iter.hasNext()) {
					com.clustercontrol.ws.xcloud.Instance instance = iter.next();
					if (instance.getCloudScopeId().equals(scope.getId()) && instance.getLocationId().equals(location.getId())) {
						locationInstances.add(instance);
						iter.remove();
					}
				}
				location.getComputeResources().updateInstances(locationInstances);
				for (Instance i: location.getComputeResources().getInstances()) {
					i.getBackup().update(instanceBackupMap.get(Arrays.asList(i.getCloudScope().getId(), i.getId())));
				}
			}
		}
		
		Map<List<String>, com.clustercontrol.ws.xcloud.StorageBackup> storageBackupMap = new HashMap<>();
		for (com.clustercontrol.ws.xcloud.StorageBackup backup: repository.getStorageBackups()) {
			storageBackupMap.put(Arrays.asList(backup.getCloudScopeId(), backup.getStorageId()), backup);
		}
		
		List<com.clustercontrol.ws.xcloud.Storage> storages = new ArrayList<>(repository.getStorages());
		for (CloudScope scope: getCloudScopes()) {
			for (Location location: scope.getLocations()) {
				List<com.clustercontrol.ws.xcloud.Storage> locationStorages = new ArrayList<>();
				Iterator<com.clustercontrol.ws.xcloud.Storage> iter = storages.iterator();
				while (iter.hasNext()) {
					com.clustercontrol.ws.xcloud.Storage storage = iter.next();
					if (storage.getCloudScopeId().equals(scope.getId()) && storage.getLocationId().equals(location.getId())) {
						locationStorages.add(storage);
						iter.remove();
					}
				}
				location.getComputeResources().updateStorages(locationStorages);
				for (Storage s: location.getComputeResources().getStorages()) {
					s.getBackup().update(storageBackupMap.get(Arrays.asList(s.getCloudScopeId(), s.getId())));
				}
			}
		}
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IHinemosManager.class) {
			return getHinemosManager();
		}
		return super.getAdapter(adapter);
	}
}
