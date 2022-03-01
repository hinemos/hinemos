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
import org.openapitools.client.model.CloudScopeInfoResponse;
import org.openapitools.client.model.HFacilityResponse;
import org.openapitools.client.model.HRepositoryResponse;
import org.openapitools.client.model.InstanceBackupResponse;
import org.openapitools.client.model.InstanceInfoResponse;
import org.openapitools.client.model.LocationInfoResponse;
import org.openapitools.client.model.StorageBackupInfoResponse;
import org.openapitools.client.model.StorageInfoResponse;

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
	
	public void updateCloudScopes(HRepositoryResponse repository) {
		if (cloudScopes == null)
			cloudScopes = new ArrayList<>();

		final List<CloudScopeInfoResponse> webCloudScopes = new ArrayList<>();
		final Map<List<String>, Set<InstanceInfoResponse>> webInstanceMap = new HashMap<List<String>, Set<InstanceInfoResponse>>();
		HRepositoryParser.parse(repository, new HRepositoryParser.Handler() {
			private CloudScopeInfoResponse currentScope;
			private LocationInfoResponse currentLocation;
			public boolean cloudScopeScope(HFacilityResponse f) {
				currentScope = f.getCloudScope();
				currentLocation = f.getLocation();
				webCloudScopes.add(currentScope);
				return true;
			};
			public boolean locationScope(HFacilityResponse f) {
				currentLocation = f.getLocation();
				return true;
			};

			public void instanceNode(HFacilityResponse f) {
				InstanceInfoResponse i = f.getInstance();
				if (!currentScope.getEntity().getCloudScopeId().equals(i.getCloudScopeId()) || !currentLocation.getId().equals(i.getLocationId())) {
					logger.warn(String.format("instance is placed in another cloudscope tree. cloudscopeId=%s, locationId=%s, instanceId=%s",
							i.getCloudScopeId(), i.getLocationId(), i.getId()));
				} else {
					List<String> key = Arrays.asList(i.getCloudScopeId(), i.getLocationId());
					Set<InstanceInfoResponse> instances = webInstanceMap.get(key);
					if (instances == null) {
						instances = new HashSet<>();
						webInstanceMap.put(key, instances);
					}
					instances.add(i);
				}
			};
		});
		
		CollectionComparator.compareCollection(cloudScopes, webCloudScopes, new CollectionComparator.Comparator<CloudScope, CloudScopeInfoResponse>() {
			public boolean match(CloudScope o1, CloudScopeInfoResponse o2) {return o1.equalValues(o2);}
			public void matched(CloudScope o1, CloudScopeInfoResponse o2) {o1.update(o2);}
			public void afterO1(CloudScope o1) {
				internalRemoveProperty(p.cloudScopes, o1, cloudScopes);
			}
			public void afterO2(CloudScopeInfoResponse o2) {
				CloudScope newCloudScope = CloudScope.convert(CloudScopes.this, o2);
				internalAddProperty(p.cloudScopes, newCloudScope, cloudScopes);
			}
		});
		
		Map<List<String>, InstanceBackupResponse> instanceBackupMap = new HashMap<>();
		for (InstanceBackupResponse backup : repository.getInstanceBackups()) {
			instanceBackupMap.put(Arrays.asList(backup.getCloudScopeId(), backup.getInstanceId()), backup);
		}

		List<InstanceInfoResponse> instances = new ArrayList<>(repository.getInstances());
		for (CloudScope scope: getCloudScopes()) {
			for (Location location: scope.getLocations()) {
				List<InstanceInfoResponse> locationInstances = new ArrayList<>();
				Iterator<InstanceInfoResponse> iter = instances.iterator();
				while (iter.hasNext()) {
					InstanceInfoResponse instance = iter.next();
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
		
		Map<List<String>, StorageBackupInfoResponse> storageBackupMap = new HashMap<>();
		for (StorageBackupInfoResponse backup: repository.getStorageBackups()) {
			storageBackupMap.put(Arrays.asList(backup.getCloudScopeId(), backup.getStorageId()), backup);
		}
		
		List<StorageInfoResponse> storages = new ArrayList<>(repository.getStorages());
		for (CloudScope scope: getCloudScopes()) {
			for (Location location: scope.getLocations()) {
				List<StorageInfoResponse> locationStorages = new ArrayList<>();
				Iterator<StorageInfoResponse> iter = storages.iterator();
				while (iter.hasNext()) {
					StorageInfoResponse storage = iter.next();
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
