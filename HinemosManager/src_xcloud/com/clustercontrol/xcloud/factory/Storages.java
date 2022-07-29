/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import static com.clustercontrol.xcloud.common.CloudConstants.Event_Storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AddStorageRequest;
import com.clustercontrol.xcloud.bean.BackupedData.BackupedDataEntry;
import com.clustercontrol.xcloud.bean.CloneBackupedStorageRequest;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.IResourceManagement.StorageSnapshot.StorageSnapshotStatusType;
import com.clustercontrol.xcloud.model.BackupedData;
import com.clustercontrol.xcloud.model.BackupedDataStore;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallable;
import com.clustercontrol.xcloud.model.ExtendedProperty;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.LocationResourceEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntryEntity;
import com.clustercontrol.xcloud.model.StorageEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.NodeInfoCache;
import com.clustercontrol.xcloud.util.NodeInfoCache.NodeInfoCacheScope;

@Transactional
public class Storages extends Resources implements IStorages {
	private static final Log logger = LogFactory.getLog(Storages.class);
	
	private InstanceEntity cachedInstanceEntity;
	private boolean doDiffCheck = true;
	
	public Storages() {
		super();
	}
	
	@Override
	public List<StorageEntity> removeStorages(final List<String> storageIds) throws CloudManagerException {
		return getUser().getCloudScope().optionCall(new CloudScopeEntity.OptionCallable<List<StorageEntity>>() {
			@Override
			public List<StorageEntity> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<StorageEntity> ret = getStorages(storageIds);
				option.getResourceManagement(getLocation(), getUser()).removeStorages(storageIds);
				return ret;
			}
		});
	}

	@Override
	public List<StorageEntity> getStorages(List<String> storageIds) throws CloudManagerException {
		if (!storageIds.isEmpty()) {
			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<StorageEntity> query = em.createNamedQuery(StorageEntity.findStorages, StorageEntity.class);
			query.setParameter("cloudScopeId", getUser().getCloudScopeId());
			query.setParameter("locationId", getLocation().getLocationId());
			query.setParameter("storageIds", storageIds);
			return query.getResultList();
		} else {
			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<StorageEntity> query = em.createNamedQuery(StorageEntity.findStoragesByLocation, StorageEntity.class);
			query.setParameter("cloudScopeId", getUser().getCloudScopeId());
			query.setParameter("locationId", getLocation().getLocationId());
			return query.getResultList();
		}
	}

	@Override
	public void attachStorage(final String instanceId, final String storageId, final List<Option> options) throws CloudManagerException {
		getUser().getCloudScope().optionExecute(new CloudScopeEntity.OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				InstanceEntity entity = getInstanceEntity(instanceId);
				option.getResourceManagement(getLocation(), getUser()).attachStorage(entity.getResourceId(), storageId, options);
			}
		});
	}

	@Override
	public void detachStorage(final List<String> storageIds) throws CloudManagerException {
		getUser().getCloudScope().optionExecute(new CloudScopeEntity.OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<StorageEntity> entities = CloudManager.singleton().getStorages(getUser(), getLocation()).getStorages(storageIds);
				for (StorageEntity entity: entities) {
					if (entity.getTargetInstanceId() != null)
						option.getResourceManagement(getLocation(), getUser()).detachStorage(entity.getTargetInstanceId(), entity.getResourceId());
				}
			}
		});
	}
	
	protected StorageEntity updateStorageEntity(final StorageEntity storage, IResourceManagement.Storage platformStorage) throws CloudManagerException {
		// 登録済みの情報を更新。
		storage.setName(platformStorage.getName() != null ? platformStorage.getName(): null);
		storage.setStorageType(platformStorage.getStorageType());
		storage.setSize(platformStorage.getSize());
		storage.setStorageStatus(platformStorage.getStorageStatus());
		storage.setStorageStatusAsPlatform(platformStorage.getStatusAsPlatform());
		
		CollectionComparator.compare(platformStorage.getExtendedProperty(), storage.getExtendedProperties().entrySet(),
			new CollectionComparator.Comparator<IResourceManagement.ExtendedProperty, Map.Entry<String, ExtendedProperty>>() {
				@Override
				public boolean match(IResourceManagement.ExtendedProperty o1, Entry<String, ExtendedProperty> o2) throws CloudManagerException {
					return o1.getName().equals(o2.getKey());
				}
				@Override
				public void matched(IResourceManagement.ExtendedProperty o1, Entry<String, ExtendedProperty> o2) throws CloudManagerException {
					o2.getValue().setDataType(o1.getDataType());
					o2.getValue().setValue(o1.getValue());
				}
				@Override
				public void afterO1(IResourceManagement.ExtendedProperty o1) throws CloudManagerException {
					ExtendedProperty ep = new ExtendedProperty();
					ep.setName(o1.getName());
					ep.setDataType(o1.getDataType());
					ep.setValue(o1.getValue());
					storage.getExtendedProperties().put(o1.getName(), ep);
				}
				@Override
				public void afterO2(Entry<String, ExtendedProperty> o2) throws CloudManagerException {
					storage.getExtendedProperties().remove(o2.getKey());
				}
		});
		
		if (platformStorage.getTargetInstanceId() == null) {
			removeStorageRelation(storage);
		} else {
			updateStorageRelation(storage, platformStorage);
		}

		return storage;
	}	
	
	protected void disableStorageEntity(StorageEntity storage) throws CloudManagerException {
		if (storage.getStorageStatus() != IResourceManagement.Storage.StorageStatus.notfound)
			storage.setStorageStatus(IResourceManagement.Storage.StorageStatus.notfound);
		
		removeStorageRelation(storage);
		
		// ディスク情報が無い場合は、削除。
		if (storage.getTargetFacilityId() == null) {
			HinemosEntityManager em = Session.current().getEntityManager();
			try (RemovedEventNotifier<StorageEntity> notifier = new RemovedEventNotifier<>(StorageEntity.class, Event_Storage, storage)) {
				List<String> snapshotIds = new ArrayList<>();
				for (StorageBackupEntryEntity entry: storage.getBackup().getEntries()) {
					snapshotIds.add(entry.getEntryId());
				}
				if (!snapshotIds.isEmpty())
					deleteStorageSnapshots(storage.getResourceId(), snapshotIds);
				em.remove(storage.getBackup());
				em.remove(storage);
				notifier.completed();
			}
			storage = null;
		}
	}
	
	protected List<StorageEntity> internalUpdateStorages(List<StorageEntity> storages, List<IResourceManagement.Storage> platformStorages) throws CloudManagerException {
		final List<StorageEntity> updateds = new ArrayList<>();
		try(NodeInfoCacheScope scope = new NodeInfoCacheScope(doDiffCheck)) {
			CollectionComparator.compare(storages, platformStorages, new CollectionComparator.Comparator<StorageEntity, IResourceManagement.Storage>() {
				@Override
				public boolean match(StorageEntity o1, IResourceManagement.Storage o2) throws CloudManagerException {
					return o1.getResourceId().equals(o2.getResourceId());
				}
				@Override
				public void matched(StorageEntity o1, IResourceManagement.Storage o2) throws CloudManagerException {
					StorageEntity updated = updateStorageEntity(o1, o2);
					if (updated != null)
						updateds.add(updated);
				}
				@Override
				public void afterO1(StorageEntity o1) throws CloudManagerException {
					if (o1.getTargetFacilityId() != null)
						removeStorageRelation(o1);
					
					disableStorageEntity(o1);
				}
				@Override
				public void afterO2(IResourceManagement.Storage o2) throws CloudManagerException {
					StorageEntity updated = addStorageEntity(o2);
					if (updated != null)
						updateds.add(updated);
					
					if (o2.getTargetInstanceId() != null) {
						updateStorageRelation(updated, o2);
					}
				}
			});
			
		}
		return updateds;
	}
	
	protected StorageEntity createStorageEntity(IResourceManagement.Storage platformStorage) throws CloudManagerException {
		// DB に追加する情報を作成。
		StorageEntity storage = new StorageEntity();
		storage.setCloudScopeId(getUser().getCloudScopeId());
		storage.setLocationId(getLocation().getLocationId());
		storage.setResourceId(platformStorage.getResourceId());
		storage.setName(platformStorage.getName());
		storage.setStorageType(platformStorage.getStorageType());
		storage.setSize(platformStorage.getSize());
		storage.setResourceTypeAsPlatform(platformStorage.getResourceTypeAsPlatform());
		storage.setStorageStatus(platformStorage.getStorageStatus());
		storage.setStorageStatusAsPlatform(platformStorage.getStatusAsPlatform());
		storage.setTargetInstanceId(platformStorage.getTargetInstanceId());
		StorageBackupEntity backup = new StorageBackupEntity();
		backup.setCloudScopeId(storage.getCloudScopeId());
		backup.setLocationId(storage.getLocationId());
		backup.setStorageId(storage.getResourceId());
		storage.setBackup(backup);
		
		for (IResourceManagement.ExtendedProperty entry: platformStorage.getExtendedProperty()) {
			ExtendedProperty ep = new ExtendedProperty();
			ep.setName(entry.getName());
			ep.setDataType(entry.getDataType());
			ep.setValue(entry.getValue());
			storage.getExtendedProperties().put(entry.getName(), ep);
		}
		
		try (AddedEventNotifier<StorageEntity> notifier = new AddedEventNotifier<>(StorageEntity.class, Event_Storage, storage)) {
			try {
				HinemosEntityManager em = Session.current().getEntityManager();
				PersistenceUtil.persist(em, storage);
				PersistenceUtil.persist(em, backup);

				notifier.setCompleted();
				return storage;
			}
			catch (EntityExistsException e) {
				throw ErrorCode.CLOUDSTORAGE_ALREADY_EXIST.cloudManagerFault(storage.getResourceId());
			}
		}
	}
	
	protected void updateStorageRelation(StorageEntity storage, IResourceManagement.Storage platformStorage) throws CloudManagerException {
		storage.setTargetInstanceId(platformStorage.getTargetInstanceId());
		if (platformStorage.getTargetInstanceId() == null)
			return;
		
		// ストレージに紐づいているインスタンスの情報から、ディスク情報を更新。
		try {
			InstanceEntity instance = getInstanceEntity(platformStorage.getTargetInstanceId());
			updateDiskInfo(instance, storage, platformStorage);
		} catch (CloudManagerException e) {
			logger.warn(e.getMessage());
		}
	}
	
	protected void updateDiskInfo(InstanceEntity instance, StorageEntity storage, IResourceManagement.Storage platformStorage) throws CloudManagerException {
		// FacilityId に紐づいているか確認。
		if (instance.getFacilityId() != null) {
			if (storage.getTargetFacilityId() != null && !instance.getFacilityId().equals(storage.getTargetFacilityId())) {
				try {
					removeDiskInfo(storage.getTargetFacilityId(), storage.getResourceId());
				} catch (FacilityNotFound e) {
					throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
				}
			}
			
			try (NodeInfoCacheScope scope = new NodeInfoCacheScope(doDiffCheck)) {
				// ディスク情報を設定。
				NodeInfo nodeInfo = NodeInfoCache.getNodeInfo(instance.getFacilityId());
				// 既に紐づいているか確認。
				NodeDiskInfo nodeDiskInfo = null;
				for (NodeDiskInfo diskInfo: nodeInfo.getNodeDiskInfo()) {
					if (storage.getResourceId().equals(diskInfo.getDeviceName())) {
						// 既に紐づいているので終了。
						nodeDiskInfo = diskInfo;
						break;
					}
				}

				// まだ、紐づいていないので追加。
				if (nodeDiskInfo == null) {
					nodeDiskInfo = new NodeDiskInfo();
					nodeDiskInfo.setDeviceType("vdisk");
					nodeDiskInfo.setDeviceName(platformStorage.getResourceId());

					Set<Integer> indexs = new HashSet<Integer>();
					for (NodeDiskInfo diskInfo: nodeInfo.getNodeDiskInfo()) {
						indexs.add(diskInfo.getDeviceIndex());
					}
					
					for (int i = 0; i < Integer.MAX_VALUE; ++i) {
						if (!indexs.contains(i)) {
							nodeDiskInfo.setDeviceIndex(i);
							break;
						}
					}
					
					nodeDiskInfo.setDeviceSize(storage.getSize().longValue());
					nodeDiskInfo.setDeviceSizeUnit("Gib");
					nodeDiskInfo.setDeviceDescription("");
					nodeDiskInfo.setDeviceDisplayName(storage.getName() == null || storage.getName().isEmpty() ? platformStorage.getDeviceName(): storage.getName());
					ArrayList<NodeDiskInfo> disklist = new ArrayList<NodeDiskInfo>(nodeInfo.getNodeDiskInfo());
					disklist.add(nodeDiskInfo);
					nodeInfo.setNodeDiskInfo(disklist);

					logger.info(String.format("Add disk. autoRegist=%b, CloudLoginUser=%s, FacilityID=%s, InstanceId=%s, AddDisk=%s, StorageId=%s", 
							ActionMode.isAutoDetection(), getUser().getLoginUserId(), nodeInfo.getFacilityId(), instance.getResourceId(), platformStorage.getDeviceName(), storage.getResourceId()));

					scope.modified(instance.getFacilityId());

					storage.setDeviceIndex(nodeDiskInfo.getDeviceIndex());
					storage.setDeviceName(nodeDiskInfo.getDeviceName());
					storage.setDeviceType(nodeDiskInfo.getDeviceType());
					storage.setTargetFacilityId(nodeInfo.getFacilityId());
				} else {
					StringBuilder changeLog = new StringBuilder();

					// nodeDiskInfo.setDeviceType
					String oldValue = nodeDiskInfo.getDeviceType();
					String newValue = "vdisk";
					if((null!=oldValue && !oldValue.equals(newValue)) || null==oldValue){
						changeLog.append("DeviceType:").append(oldValue).append("->").append(newValue).append(";");
						nodeDiskInfo.setDeviceType(newValue);
					}

					//nodeDiskInfo.setDeviceName(platformStorage.getResourceId());
					oldValue = nodeDiskInfo.getDeviceName();
					newValue = platformStorage.getResourceId();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						changeLog.append("DeviceName:").append(oldValue).append("->").append(newValue).append(";");
						nodeDiskInfo.setDeviceName(newValue);
					}

					//nodeDiskInfo.setDeviceSize(storage.getSize());
					if(
						(nodeDiskInfo.getDeviceSize() != null && storage.getSize() == null) ||
						(nodeDiskInfo.getDeviceSize() == null && storage.getSize() != null) ||
						(nodeDiskInfo.getDeviceSize() != null && storage.getSize() != null && !(nodeDiskInfo.getDeviceSize().longValue() == storage.getSize().longValue()))
						) {
						changeLog.append("DeviceSize:").append(nodeDiskInfo.getDeviceSize()).append("->").append(storage.getSize()).append(";");
						nodeDiskInfo.setDeviceSize(storage.getSize().longValue());
					}

					//nodeDiskInfo.setDeviceSizeUnit("Gib");
					oldValue = nodeDiskInfo.getDeviceSizeUnit();
					newValue = "Gib";
					if((null!=oldValue && !oldValue.equals(newValue)) || null==oldValue){
						changeLog.append("DeviceSizeUnit:").append(oldValue).append("->").append(newValue).append(";");
						nodeDiskInfo.setDeviceSizeUnit(newValue);
					}

					//nodeDiskInfo.setDeviceDisplayName(storage.getName() == null || storage.getName().isEmpty() ? platformStorage.getDeviceName(): storage.getName());
					oldValue = nodeDiskInfo.getDeviceDisplayName();
					newValue = storage.getName() == null || storage.getName().isEmpty() ? platformStorage.getDeviceName(): storage.getName();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						changeLog.append("DeviceDisplayName:").append(oldValue).append("->").append(newValue).append(";");
						nodeDiskInfo.setDeviceDisplayName(newValue);
					}

					if(0<changeLog.length()){
						logger.info(String.format("Update disk. autoRegist=%b, CloudLoginUser=%s, FacilityID=%s, InstanceId=%s, AddDisk=%s, StorageId=%s, log=%s", 
								ActionMode.isAutoDetection(), getUser().getLoginUserId(), nodeInfo.getFacilityId(), instance.getResourceId(), platformStorage.getDeviceName(), storage.getResourceId(), changeLog));
					}

					scope.modified(instance.getFacilityId());

					storage.setDeviceIndex(nodeDiskInfo.getDeviceIndex());
					storage.setDeviceName(nodeDiskInfo.getDeviceName());
					storage.setDeviceType(nodeDiskInfo.getDeviceType());
					storage.setTargetFacilityId(nodeInfo.getFacilityId());
					storage.setTargetInstanceId(platformStorage.getTargetInstanceId());
				}
			} catch (FacilityNotFound e) {
				logger.warn(e.getMessage());
			}
		} else {
			if (storage.getTargetFacilityId() != null) {
				try {
					removeDiskInfo(storage.getTargetFacilityId(), storage.getResourceId());
				} catch (FacilityNotFound e) {
					throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
				}
			}

			storage.setDeviceIndex(null);
			storage.setDeviceName(null);
			storage.setDeviceType(null);
			storage.setTargetFacilityId(null);
		}
	}
	
	protected void removeDiskInfo(String facilityId, String storageId) throws CloudManagerException, FacilityNotFound {
		NodeInfo nodeInfo = null;
		try {
			nodeInfo = new RepositoryControllerBean().getNode(facilityId);
		} catch (HinemosUnknown e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}

		if (nodeInfo != null && nodeInfo.getNodeDiskInfo() != null) {
			for (NodeDiskInfo disk: nodeInfo.getNodeDiskInfo()) {
				if (storageId.equals(disk.getDeviceName())) {
					
					InstanceEntity instance = null;
					if (ActionMode.isAutoDetection()) {
						try {
							instance = getInstanceByFacilityId(facilityId);
						} catch (Exception e) {
							logger.warn(e.toString(), e);
						}
					}

					ArrayList<NodeDiskInfo> disklist = new ArrayList<NodeDiskInfo>(nodeInfo.getNodeDiskInfo());
					disklist.remove(disk);
					nodeInfo.setNodeDiskInfo(disklist);

					logger.info(String.format("Remove disk. autoRegist=%b, CloudLoginUser= %s, FacilityID= %s, InstanceId= %s, DelDisk=%s, StorageId=%s", 
							ActionMode.isAutoDetection(), getUser().getLoginUserId(), nodeInfo.getFacilityId(), instance == null ? "none": instance.getResourceId(), disk.getDeviceName(), storageId));
					break;
				}
			}
		}
	}
	
	protected void removeStorageRelation(StorageEntity storage) throws CloudManagerException {
		storage.setTargetInstanceId(null);

		if (storage.getTargetFacilityId() == null)
			return;
		
		try {
			removeDiskInfo(storage.getTargetFacilityId(), storage.getResourceId());
		}catch (FacilityNotFound e) {
			// ノードがない場合も続行。
			logger.warn(e.getMessage(), e);
		}

		storage.setTargetFacilityId(null);
		storage.setDeviceIndex(null);
		storage.setDeviceName(null);
		storage.setDeviceType(null);
	}

	@Override
	public StorageEntity addStorage(final AddStorageRequest request) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<StorageEntity>() {
			@Override
			public StorageEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				CloudLoginUserEntity user = getUser();
				LocationEntity location = getLocation();
				IResourceManagement.Storage platformStorage = option.getResourceManagement(location, user).addStorage(request);
				return addStorageEntity(platformStorage);	
			}
		});
	}
	
	protected StorageEntity addStorageEntity(IResourceManagement.Storage platformStorage) throws CloudManagerException {
		StorageEntity storage;
		try {
			// AWS ボリュームが追加。
			storage = createStorageEntity(platformStorage);
		} catch (CloudManagerException e) {
			// 別の処理が作成済みらしい。
			if (ErrorCode.CLOUDSTORAGE_ALREADY_EXIST.match(e)) {
				HinemosEntityManager em = Session.current().getEntityManager();
				storage = em.find(StorageEntity.class, new LocationResourceEntity.LocationResourceEntityPK(getUser().getCloudScopeId(), getLocation().getLocationId(), platformStorage.getResourceId()), ObjectPrivilegeMode.READ);
				if (storage == null) {
					throw e;
				}
				return storage;
			}
			throw e;
		}

		updateStorageRelation(storage, platformStorage);
		
		return storage;
	}

	@Override
	public StorageBackupEntity takeStorageSnapshot(final String storageId, final String name, final String description, final List<Option> options) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<StorageBackupEntity>() {
			@Override
			public StorageBackupEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<StorageEntity> storageEntities = getStorages(Arrays.asList(storageId));
				
				StorageEntity storageEntity;
				if (!storageEntities.isEmpty()) {
					storageEntity = storageEntities.get(0);
				} else {
					throw ErrorCode.CLOUDSTORAGE_NOT_FOUND.cloudManagerFault(storageId);
				}
				
				final StorageBackupEntryEntity backupEntry = new StorageBackupEntryEntity();
				IResourceManagement.StorageSnapshot snapshot = option.getResourceManagement(getLocation(), getUser()).takeStorageSnapshot(storageEntity.getResourceId(), name, description, 
						new BackupedDataStore() {
							@Override
							protected Map<String, BackupedData> getBackedupDataMap() {
								return backupEntry.getBackupedData();
							}
						}, options);
				
				backupEntry.setCloudScopeId(Storages.this.getCloudScope().getId());
				backupEntry.setLocationId(Storages.this.getLocation().getLocationId());
				backupEntry.setStorageId(storageId);
				backupEntry.setEntryId(snapshot.getResourceId());
				backupEntry.setName(snapshot.getName());
				backupEntry.setDescription(snapshot.getDescription());
				
				backupEntry.setStatus(snapshot.getState());
				backupEntry.setStatusAsPlatform(snapshot.getStatusAsPlatform());
				if (snapshot.getCreateTime() != null) {
					backupEntry.setCreateTime(snapshot.getCreateTime());
				} else {
					backupEntry.setCreateTime(new Date().getTime());
				}
				
				{
					BackupedData data = new BackupedData();
					data.setName(backup_storageName);
					data.setValue(storageEntity.getName());
					backupEntry.getBackupedData().put(backup_storageName, data);
				}
				{
					BackupedData data = new BackupedData();
					data.setName(backup_storageSize);
					data.setValue(storageEntity.getSize().toString());
					backupEntry.getBackupedData().put(backup_storageName, data);
				}

				backupEntry.setStorageBackup(storageEntity.getBackup());
				
				storageEntity.getBackup().getEntries().add(backupEntry);
				
				List<StorageBackupEntryEntity> list  = new ArrayList<>(storageEntity.getBackup().getEntries());
				Collections.sort(list, new Comparator<StorageBackupEntryEntity>() {
					@Override
					public int compare(StorageBackupEntryEntity o1, StorageBackupEntryEntity o2) {
						return o2.getCreateTime().compareTo(o1.getCreateTime());
					}
				});
				
				int maxcount = HinemosPropertyCommon.xcloud_storage_snapshot_count_max.getIntegerValue();
				if (maxcount < list.size()) {
					final List<StorageBackupEntryEntity> removed = new ArrayList<>();
					for (int i = maxcount; i < list.size(); ++i) {
						StorageBackupEntryEntity entry = list.get(i);
						storageEntity.getBackup().getEntries().remove(entry);
						Session.current().getEntityManager().remove(entry);
						removed.add(entry);
					}
					try {
						option.getResourceManagement(getLocation(), getUser()).deleteStorageSnapshots(removed);
					} catch(CloudManagerException e) {
						logger.warn(e.getMessage(), e);
					}
				}
				
				return storageEntity.getBackup();
			}
		});
	}

	@Override
	public List<StorageBackupEntryEntity> deleteStorageSnapshots(final String storageId, final List<String> snapshotIds) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<List<StorageBackupEntryEntity>>() {
			@Override
			public List<StorageBackupEntryEntity> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<StorageEntity> storageEntities = getStorages(Arrays.asList(storageId));
				
				final StorageEntity storageEntity;
				if (!storageEntities.isEmpty()) {
					storageEntity = storageEntities.get(0);
				} else {
					throw ErrorCode.CLOUDINSTANCE_NOT_FOUND.cloudManagerFault(storageId);
				}

				final List<StorageBackupEntryEntity> removed = new ArrayList<>();
				CollectionComparator.compare(storageEntity.getBackup().getEntries(), snapshotIds, new CollectionComparator.Comparator<StorageBackupEntryEntity, String>() {
					@Override
					public boolean match(StorageBackupEntryEntity o1, String o2) throws CloudManagerException {
						return o1.getEntryId().equals(o2);
					}
					@Override
					public void matched(StorageBackupEntryEntity o1, String o2) throws CloudManagerException {
						storageEntity.getBackup().getEntries().remove(o1);
						Session.current().getEntityManager().remove(o1);
						removed.add(o1);
					}
				});

				try {
					IResourceManagement management = option.getResourceManagement(getLocation(), getUser());
					management.deleteStorageSnapshots(removed);
				} catch(CloudManagerException e) {
					logger.warn(e.getMessage(), e);
				}
				return removed;
			}
		});
	}
	
	@Override
	public StorageEntity cloneBackupedStorage(final CloneBackupedStorageRequest request) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<StorageEntity>() {
			@Override
			public StorageEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				try (TransactionScope transaction = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					List<StorageEntity> storageEntities = getStorages(Arrays.asList(request.getStorageId()));
					final StorageEntity storageEntity;
					if (!storageEntities.isEmpty()) {
						storageEntity = storageEntities.get(0);
					} else {
						throw ErrorCode.CLOUDSTORAGE_NOT_FOUND.cloudManagerFault(request.getStorageId());
					}
					
					StorageBackupEntryEntity backupedEntry = null;
					for (StorageBackupEntryEntity entry: storageEntity.getBackup().getEntries()) {
						if (entry.getEntryId().equals(request.getStorageSnapshotId())) {
							backupedEntry = entry;
							break;
						}
					}
					if (backupedEntry == null)
						throw ErrorCode.CLOUDINSTANCE_NOT_FOUND.cloudManagerFault(request.getStorageId());
					
					Map<String, String> backupedData = new HashMap<>();
					for (BackupedData data: backupedEntry.getBackupedData().values()) {
						backupedData.put(data.getName(), data.getValue());
					}
					for (BackupedDataEntry entry: request.getModifiedData().getEntries()) {
						backupedData.put(entry.getName(), entry.getValue());
					}
					IResourceManagement.Storage platformStorage = option.getResourceManagement(getLocation(), getUser()).cloneBackupedStorage(backupedEntry, backupedData, request.getOptions());
					StorageEntity newStorageEntity = addStorageEntity(platformStorage);	
					
					transaction.complete();

					return newStorageEntity;
				}
			}
		});
	}
	
	@Override
	public StorageEntity getStorage(String storageId) throws CloudManagerException {
		List<StorageEntity> storages = getStorages(Arrays.asList(storageId));
		if (storages.isEmpty())
			throw ErrorCode.CLOUDSTORAGE_NOT_FOUND.cloudManagerFault(storageId);
		return storages.get(0);
	}
	
	@Override
	public Storages setCachedInstanceEntity(InstanceEntity instance) throws CloudManagerException {
		cachedInstanceEntity = instance;
		return this;
	}
	
	protected InstanceEntity getInstanceEntity(String instanceId) throws CloudManagerException {
		if (cachedInstanceEntity != null && cachedInstanceEntity.getResourceId().equals(instanceId)) {
			return cachedInstanceEntity;
		} else {
			return CloudManager.singleton().getInstances(getUser(), getLocation()).getInstance(instanceId);
		}
	}
	
	protected InstanceEntity getInstanceByFacilityId(String facilityId) throws CloudManagerException {
		if (cachedInstanceEntity != null && facilityId.equals(cachedInstanceEntity.getFacilityId())) {
			return cachedInstanceEntity;
		} else {
			return CloudManager.singleton().getInstances(getUser(), getLocation()).getInstanceByFacilityId(facilityId);
		}
	}
	
	@Override
	public IStorages setDoDiffCheck(boolean doDiffCheck) {
		this.doDiffCheck = doDiffCheck;
		return this;
	}
	
	@Override
	public List<StorageEntity> updateStorages(List<String> storageIds) throws CloudManagerException {
		List<StorageEntity> storages = null;	
		ILock lock = CloudUtil.getLock(Storages.class.getName(), getCloudScope().getId(),
				getLocation().getLocationId());
		logger.debug(String.format("updateStorages(): getLock cloudScopeId=%s, locationId=%s", getCloudScope().getId(),
				getLocation().getLocationId()));
		storages = getUser().getCloudScope().optionCall(new CloudScopeEntity.OptionCallable<List<StorageEntity>>() {
			@Override
			public List<StorageEntity> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IResourceManagement rm = getResourceManagement(option);
				boolean getLock = false;
				try {
					// 同じ情報を重複してDBに書き込もうとしてEntityExistExceptionが発生する可能性があるので、
					// ストレージの更新処理はクラウドスコープID単位で排他処理にする
					// ただしキャッシュの更新の場合はロックは取得しない
					if (rm instanceof CacheResourceManagement) {
						logger.debug(String.format(
								"updateStorages(): lock not acquired since its cached. cloudScopeId=%s, locationId=%s",
								getCloudScope().getId(), getLocation().getLocationId()));
					} else {
						// クラウドスコープID単位で書き込みロックを取得
						lock.writeLock();
						getLock = true;
						logger.debug(String.format("updateStorages(): writeLock cloudScopeId=%s, locationId=%s",
								getCloudScope().getId(), getLocation().getLocationId()));
					}
					List<IResourceManagement.Storage> storages = getResourceManagement(option).getStorages(storageIds);
					return internalUpdateStorages(getStorages(storageIds), storages);
				} finally {
					if (getLock) {
						lock.writeUnlock();
						logger.debug(String.format("updateStorages(): writeUnlock cloudScopeId=%s, locationId=%s",
								getCloudScope().getId(), getLocation().getLocationId()));
					}
				}
			}
		});

		return storages;
	}

	@Override
	public List<StorageBackupEntity> updateStorageBackups(List<String> storageIds) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<List<StorageBackupEntity>>() {
			@Override
			public List<StorageBackupEntity> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				TypedQuery<StorageBackupEntity> query;
				if (storageIds == null || storageIds.isEmpty()) {
					query = Session.current().getEntityManager().createNamedQuery(StorageBackupEntity.findStorageBackups, StorageBackupEntity.class);
					query.setParameter("cloudScopeId", getCloudScope().getId());
					query.setParameter("locationId", getLocation().getLocationId());
				} else {
					query = Session.current().getEntityManager().createNamedQuery(StorageBackupEntity.findStorageBackupsByStorageIds, StorageBackupEntity.class);
					query.setParameter("cloudScopeId", getCloudScope().getId());
					query.setParameter("locationId", getLocation().getLocationId());
					query.setParameter("storageIds", storageIds);
				}
				
				List<StorageBackupEntryEntity> entries = new ArrayList<>();
				List<StorageBackupEntity> backups = query.getResultList();
				for (StorageBackupEntity backup: backups) {
					for (StorageBackupEntryEntity entry: backup.getEntries()) {
						entries.add(entry);
					}
				}
				
				List<IResourceManagement.StorageSnapshot> snapshots;
				try {
					snapshots = getResourceManagement(option).getStorageSnapshots(entries);
				} catch (UnsupportedOperationException e) {
					//Azure、Hyper-Vなどストレージの機能がないプラットファームの場合
					return Collections.emptyList();
				}
				
				for (StorageBackupEntity backup: backups) {
					CollectionComparator.compare(backup.getEntries(), snapshots, new CollectionComparator.Comparator<StorageBackupEntryEntity, IResourceManagement.StorageSnapshot>() {
						@Override
						public boolean match(StorageBackupEntryEntity o1, IResourceManagement.StorageSnapshot o2) throws CloudManagerException {
							return o1.getEntryId().equals(o2.getResourceId());
						}
						@Override
						public void matched(final StorageBackupEntryEntity o1, IResourceManagement.StorageSnapshot o2) throws CloudManagerException {
							o1.setName(o2.getName());
							o1.setDescription(o2.getDescription());
							o1.setStatus(o2.getState());
							o1.setStatusAsPlatform(o2.getStatusAsPlatform());
							if (o2.getCreateTime() != null)
								o1.setCreateTime(o2.getCreateTime());
						}
						@Override
						public void afterO1(StorageBackupEntryEntity o1) throws CloudManagerException {
							o1.setStatus(StorageSnapshotStatusType.missing);
							o1.setStatusAsPlatform("");
						}
					});
				}
				return backups;
			}
		});
	}
	
	private CacheResourceManagement cacheRm;
	
	protected IResourceManagement getResourceManagement(ICloudOption option) {
		if (cacheRm == null) {
			return option.getResourceManagement(getLocation(), getUser());
		} else {
			return cacheRm;
		}
	}

	@Override
	public Storages setCacheResourceManagement(CacheResourceManagement rm) {
		this.cacheRm = rm;
		return this;
	}
}
