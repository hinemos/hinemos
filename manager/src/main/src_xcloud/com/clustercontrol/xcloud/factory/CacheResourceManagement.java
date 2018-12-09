/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AddInstanceRequest;
import com.clustercontrol.xcloud.bean.AddStorageRequest;
import com.clustercontrol.xcloud.bean.ModifyInstanceRequest;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.model.BackupedDataStore;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionExecutor;
import com.clustercontrol.xcloud.model.InstanceBackupEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity.InstanceBackupEntryEntityPK;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntryEntity;
import com.clustercontrol.xcloud.model.StorageEntity;
import com.clustercontrol.xcloud.util.CloudUtil;

public class CacheResourceManagement implements IResourceManagement {
	private LocationEntity location;
	private CloudLoginUserEntity cloudLoginUser;
	
	private IResourceManagement.Location resourceHierarchy;
	private Map<String, IResourceManagement.Instance> instances = new HashMap<>();
	private Map<String, IResourceManagement.InstanceSnapshot> instanceSnapshots = new HashMap<>();
	private Map<InstanceBackupEntryEntityPK, InstanceBackupEntryEntity> markedInstanceSnapshots = new HashMap<>();
	private Map<String, IResourceManagement.Storage> storages = new HashMap<>();
	private Map<String, IResourceManagement.StorageSnapshot> storageSnapshots = new HashMap<>();
	
	public CacheResourceManagement() {
	}
	
	public static CacheResourceManagement create(LocationEntity location, CloudLoginUserEntity cloudLoginUser) {
		CacheResourceManagement rm = new CacheResourceManagement();
		rm.setAccessDestination(location, cloudLoginUser);
		return rm;
	}
	
	public void update() throws CloudManagerException {
		if (!markedInstanceSnapshots.isEmpty()) {
			try {
				getCloudLoginUser().getCloudScope().optionExecute(new OptionExecutor() {
					@Override
					public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
						IResourceManagement rm = option.getResourceManagement(getLocation(), getCloudLoginUser());
						rm.deleteInstanceSnapshots(new ArrayList<>(markedInstanceSnapshots.values()));
					}
				});
			} catch(UnsupportedOperationException e) {
				// This method is called during instance update and the UnsupportedOperationException(on ESXi only) should be ignored
			}
		}
	}

	@Override
	public void setAccessDestination(LocationEntity location, CloudLoginUserEntity cloudLoginUser) {
		this.location = location;
		this.cloudLoginUser = cloudLoginUser;
	}

	@Override
	public LocationEntity getLocation() {
		return location;
	}

	@Override
	public CloudLoginUserEntity getCloudLoginUser() {
		return cloudLoginUser;
	}

	@Override
	public void disconnect() {
	}
	
	public void selectResourceHierarchy() throws CloudManagerException {
		getCloudLoginUser().getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IResourceManagement rm = option.getResourceManagement(getLocation(), getCloudLoginUser());
				CacheResourceManagement.this.resourceHierarchy = rm.getResourceHierarchy();
				setInstances(resourceHierarchy.getInstances());
			}
		});
	}
	
	@Override
	public Location getResourceHierarchy() throws CloudManagerException {
		return resourceHierarchy;
	}

	@Override
	public Instance addInstance(AddInstanceRequest request) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeInstances(List<String> instanceIds) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}
	
	public void selectInstances(String...instanceIds) throws CloudManagerException {
		getCloudLoginUser().getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IResourceManagement rm = option.getResourceManagement(getLocation(), getCloudLoginUser());
				setInstances(rm.getInstances(Arrays.asList(instanceIds)));
			}
		});
	}
	@Override
	public List<Instance> getInstances(List<String> instanceIds) throws CloudManagerException {
		if (instanceIds.isEmpty()) {
			return new ArrayList<>(instances.values());
		} else {
			List<IResourceManagement.Instance> list = new ArrayList<>();
			for (String instanceId: instanceIds) {
				if (instances.containsKey(instanceId)) {
					list.add(instances.get(instanceId));
				}
			}
			return list;
		}
	}
	public void setInstances(List<IResourceManagement.Instance> instances) {
		Map<String, IResourceManagement.Instance> map = new HashMap<>();
		for (IResourceManagement.Instance instance: instances) {
			map.put(instance.getResourceId(), instance);
		}
		this.instances = map;
	}

	@Override
	public Instance modifyInstance(ModifyInstanceRequest request) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void powerOnInstances(List<String> instanceIds) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void powerOffInstances(List<String> instanceIds) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void suspendInstances(List<String> instanceIds) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rebootInstances(List<String> instanceIds) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InstanceSnapshot takeInstanceSnapshot(String instanceId, String name, String description,
			BackupedDataStore backup, List<Option> options) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteInstanceSnapshots(List<InstanceBackupEntryEntity> entries) throws CloudManagerException {
		for (InstanceBackupEntryEntity snapshot: entries) {
			markedInstanceSnapshots.put(snapshot.getId(), snapshot);
		}
	}
	
	public void selectInstanceSnapshots(InstanceBackupEntryEntity...entries) throws CloudManagerException {
		getCloudLoginUser().getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IResourceManagement rm = option.getResourceManagement(getLocation(), getCloudLoginUser());
				List<IResourceManagement.InstanceSnapshot> images;
				if (entries.length == 0) {
					List<InstanceEntity> instances = CloudManager.singleton().getInstances(getCloudLoginUser(), location).getInstances(CloudUtil.emptyList(String.class));
					
					List<String> queryInstanceIds = instances.stream().map(InstanceEntity::getResourceId).collect(Collectors.toList());
					if (!queryInstanceIds.isEmpty()) {
						TypedQuery<InstanceBackupEntity> query = Session.current().getEntityManager().createNamedQuery("findInstanceBackupsByInstanceIds", InstanceBackupEntity.class);
						query.setParameter("cloudScopeId", getCloudLoginUser().getCloudScope().getId())
							.setParameter("locationId", getLocation().getLocationId())
							.setParameter("instanceIds", queryInstanceIds);
						
						List<InstanceBackupEntryEntity> entries = query.getResultList().stream().flatMap((v)->v.getEntries().stream()).collect(Collectors.toList());
						
						images = rm.getInstanceSnapshots(entries);
					} else {
						images = Collections.emptyList();
					}
				} else {
					images = rm.getInstanceSnapshots(Arrays.asList(entries));
				}
				
				setInstanceSnapshots(images);
			}
		});
	}
	public void deleteInstanceSnapshots(InstanceBackupEntryEntity snapshot) throws CloudManagerException {
		markedInstanceSnapshots.put(snapshot.getId(), snapshot);
	}
	@Override
	public List<IResourceManagement.InstanceSnapshot> getInstanceSnapshots(List<InstanceBackupEntryEntity> entries) {
		if (entries.isEmpty()) {
			return instanceSnapshots.values().stream().collect(Collectors.toList());
		} else {
			List<IResourceManagement.InstanceSnapshot> list = new ArrayList<>();
			for (InstanceBackupEntryEntity snapshot: entries) {
				if (instanceSnapshots.containsKey(snapshot.getEntryId())) {
					list.add(instanceSnapshots.get(snapshot.getEntryId()));
				}
			}
			return list;
		}
	}
	public void setInstanceSnapshots(List<IResourceManagement.InstanceSnapshot> instanceSnapshots) {
		Map<String, IResourceManagement.InstanceSnapshot> map = new HashMap<>();
		for (IResourceManagement.InstanceSnapshot snapshot: instanceSnapshots) {
			map.put(snapshot.getResourceId(), snapshot);
		}
		this.instanceSnapshots = map;
	}

	@Override
	public Instance cloneBackupedInstance(InstanceBackupEntryEntity entry, Map<String, String> backupedData,
			List<Option> options) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Storage addStorage(AddStorageRequest request) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void attachStorage(String instanceId, String storageId, List<Option> options) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void detachStorage(String instanceId, String storageId) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeStorages(List<String> storageIds) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}
	
	public void selectStorage(String...storageIds) throws CloudManagerException {
		getCloudLoginUser().getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IResourceManagement rm = option.getResourceManagement(getLocation(), getCloudLoginUser());
				setStorages(rm.getStorages(Arrays.asList(storageIds)));
			}
		});
	}
	@Override
	public List<Storage> getStorages(List<String> storageIds) {
		if (storageIds.isEmpty()) {
			return new ArrayList<>(storages.values());
		} else {
			List<IResourceManagement.Storage> list = new ArrayList<>();
			for (String storageId: storageIds) {
				if (storages.containsKey(storageId)) {
					list.add(storages.get(storageId));
				}
			}
			return list;
		}
	}
	public void setStorages(List<IResourceManagement.Storage> storages) {
		Map<String, IResourceManagement.Storage> map = new HashMap<>();
		for (IResourceManagement.Storage storage: storages) {
			map.put(storage.getResourceId(), storage);
		}
		this.storages = map;
	}

	@Override
	public StorageSnapshot takeStorageSnapshot(String storageId, String name, String description,
			BackupedDataStore backup, List<Option> options) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteStorageSnapshots(List<StorageBackupEntryEntity> entries) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}
	
	public void selectStorageSnapshot(StorageBackupEntryEntity...entries) throws CloudManagerException {
		getCloudLoginUser().getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IResourceManagement rm = option.getResourceManagement(location, getCloudLoginUser());
				List<IResourceManagement.StorageSnapshot> images;
				if (entries.length == 0) {
					List<StorageEntity> instances = CloudManager.singleton().getStorages(getCloudLoginUser(), getLocation()).getStorages(CloudUtil.emptyList(String.class));
					
					List<String> queryStorageIds = instances.stream().map(StorageEntity::getResourceId).collect(Collectors.toList());
					if (!queryStorageIds.isEmpty()) {
						TypedQuery<StorageBackupEntity> query = Session.current().getEntityManager().createNamedQuery(StorageBackupEntity.findStorageBackupsByStorageIds, StorageBackupEntity.class);
						query.setParameter("cloudScopeId", getCloudLoginUser().getCloudScopeId());
						query.setParameter("locationId", getLocation().getLocationId());
						query.setParameter("storageIds", queryStorageIds);
						
						List<StorageBackupEntryEntity> entries = query.getResultList().stream().flatMap((v)->v.getEntries().stream()).collect(Collectors.toList());
						
						images = rm.getStorageSnapshots(entries);
					} else {
						images = Collections.emptyList();
					}
				} else {
					images = rm.getStorageSnapshots(Arrays.asList(entries));
				}
				
				setStorageSnapshots(images);
			}
		});
	}
	@Override
	public List<IResourceManagement.StorageSnapshot> getStorageSnapshots(List<StorageBackupEntryEntity> entries) {
		if (entries.isEmpty()) {
			return new ArrayList<>(storageSnapshots.values());
		} else {
			List<IResourceManagement.StorageSnapshot> list = new ArrayList<>();
			for (StorageBackupEntryEntity snapshot: entries) {
				if (storageSnapshots.containsKey(snapshot.getEntryId())) {
					list.add(storageSnapshots.get(snapshot.getEntryId()));
				}
			}
			return list;
		}
	}
	public void setStorageSnapshots(List<IResourceManagement.StorageSnapshot> storageSnapshots) {
		Map<String, IResourceManagement.StorageSnapshot> map = new HashMap<>();
		for (IResourceManagement.StorageSnapshot snapshot: storageSnapshots) {
			map.put(snapshot.getResourceId(), snapshot);
		}
		this.storageSnapshots = map;
	}

	@Override
	public Storage cloneBackupedStorage(StorageBackupEntryEntity entry, Map<String, String> backupedData,
			List<Option> options) throws CloudManagerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Network> getNetworks() throws CloudManagerException {
		throw new UnsupportedOperationException();
	}
}
