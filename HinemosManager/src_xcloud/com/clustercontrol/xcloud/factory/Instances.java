/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AddInstanceRequest;
import com.clustercontrol.xcloud.bean.BackupedData.BackupedDataEntry;
import com.clustercontrol.xcloud.bean.CloneBackupedInstanceRequest;
import com.clustercontrol.xcloud.bean.Filter;
import com.clustercontrol.xcloud.bean.InstanceStatus;
import com.clustercontrol.xcloud.bean.ModifyInstanceRequest;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.bean.Tag;
import com.clustercontrol.xcloud.bean.TagType;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.IResourceManagement.InstanceSnapshot;
import com.clustercontrol.xcloud.factory.IResourceManagement.InstanceSnapshot.InstanceSnapshotStatusType;
import com.clustercontrol.xcloud.factory.monitors.InstanceMonitorService;
import com.clustercontrol.xcloud.model.BackupedData;
import com.clustercontrol.xcloud.model.BackupedDataStore;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallable;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionExecutor;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.RepositoryUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@Transactional
public class Instances extends Resources implements IInstances {
	private Boolean nodeRegist = HinemosPropertyCommon.xcloud_autoregist_node_instance.getBooleanValue();
	private Boolean nodeDelete = HinemosPropertyCommon.xcloud_autodelete_node_instance.getBooleanValue();
	private Boolean instanceDelete = HinemosPropertyCommon.xcloud_autodelete_instance.getBooleanValue();
	private CacheResourceManagement cacheRm;
	
	public Instances() {
	}
	
	@Override
	@Transactional(Transactional.TransactionOption.Supported)
	public Instances setNodeRegist(boolean nodeRegist) {
		this.nodeRegist = nodeRegist;
		return this;
	}

	@Override
	@Transactional(Transactional.TransactionOption.Supported)
	public Instances setInstanceDelete(boolean instanceDelete) {
		this.instanceDelete = instanceDelete;
		return this;
	}

	@Override
	@Transactional(Transactional.TransactionOption.Supported)
	public Instances setNodeDelete(boolean nodeDelete) {
		this.nodeDelete = nodeDelete;
		return this;
	}

	@Override
	public InstanceEntity addInstance(final AddInstanceRequest request) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<InstanceEntity>() {
			@Override
			public InstanceEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				InstanceEntity newInstanceEntity;
				IResourceManagement.Instance platformInstance;
				try (TransactionScope transaction = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					
					CloudLoginUserEntity user = getUser();
					LocationEntity location = getLocation();
					platformInstance = option.getResourceManagement(location, user).addInstance(request);
					
					newInstanceEntity = InstanceUpdater.updator().setNodeRegist(nodeRegist).setInstanceDelete(instanceDelete).addNewInstanceWithLock(
							getLocation(),
							getUser(),
							request.getInstanceName(), request.getMemo(), request.getTags(), platformInstance);
					
					transaction.complete();
				}
				
				// ツリーの更新
				LocationRepositoryUpdater.updateRepositoryNotDeleting(getLocation(), getUser(), platformInstance.getLocation(), null);
				
				// ノードの自動割り当て
				RepositoryUtil.autoAssgineNodesToScope(getCloudScope(), Arrays.asList(newInstanceEntity));
				
				InstanceMonitorService.getSingleton().startMonitor(
						getCloudScope().getCloudScopeId(),
						getLocation().getLocationId(),
						newInstanceEntity.getResourceId(),
						Session.current().getContext(),
						InstanceStatus.running, InstanceStatus.suspend, InstanceStatus.terminated, InstanceStatus.stopped);

				return newInstanceEntity;
			}
		});
	}

	@Override
	public void powerOnInstances(final List<String> instanceIds) throws CloudManagerException {
		getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getResourceManagement(getLocation(), getUser()).powerOnInstances(instanceIds);
				
				for (String instanceId: instanceIds) {
					InstanceMonitorService.getSingleton().startMonitor(
							getCloudScope().getCloudScopeId(),
							getLocation().getLocationId(),
							instanceId,
							Session.current().getContext(),
							InstanceStatus.running, InstanceStatus.terminated);
					if (HinemosPropertyCommon.xcloud_power_node_update_validflag.getBooleanValue()) {
						InstanceEntity instanceEntity = getInstance(instanceId);
						CloudUtil.updateValidFlg(instanceEntity.getFacilityId(), true);
					}
				}
			}
		});
	}

	@Override
	public void powerOffInstances(final List<String> instanceIds) throws CloudManagerException {
		getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getResourceManagement(getLocation(), getUser()).powerOffInstances(instanceIds);
				
				for (String instanceId: instanceIds) {
					InstanceMonitorService.getSingleton().startMonitor(
							getCloudScope().getCloudScopeId(),
							getLocation().getLocationId(),
							instanceId,
							Session.current().getContext(),
							InstanceStatus.terminated, InstanceStatus.stopped);
					if (HinemosPropertyCommon.xcloud_power_node_update_validflag.getBooleanValue()) {
						InstanceEntity instanceEntity = getInstance(instanceId);
						CloudUtil.updateValidFlg(instanceEntity.getFacilityId(), false);
					}
				}
			}
		});
	}
	
	@Override
	public void suspendInstances(final List<String> instanceIds) throws CloudManagerException {
		getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getResourceManagement(getLocation(), getUser()).suspendInstances(instanceIds);
				
				for (String instanceId: instanceIds) {
					InstanceMonitorService.getSingleton().startMonitor(
							getCloudScope().getCloudScopeId(),
							getLocation().getLocationId(),
							instanceId,
							Session.current().getContext(),
							InstanceStatus.suspend, InstanceStatus.terminated, InstanceStatus.stopped);
					if (HinemosPropertyCommon.xcloud_power_node_update_validflag.getBooleanValue()) {
						InstanceEntity instanceEntity = getInstance(instanceId);
						CloudUtil.updateValidFlg(instanceEntity.getFacilityId(), false);
					}
				}
			}
		});
	}
	
	@Override
	public List<InstanceEntity> getInstances(List<String> instanceIds) {
		if (!instanceIds.isEmpty()) {
			HinemosEntityManager em = Session.current().getEntityManager();
			int start_idx = 0;
			int end_idx = 0;
			List<InstanceEntity> instances = new ArrayList<>();
			while(start_idx < instanceIds.size()){
				TypedQuery<InstanceEntity> query =  em.createNamedQuery(InstanceEntity.findInstancesByInstanceIds, InstanceEntity.class);
				query.setParameter("cloudScopeId", getCloudScope().getId());
				query.setParameter("locationId", getLocation().getLocationId());
				end_idx = start_idx + CloudUtil.SQL_PARAM_NUMBER_THRESHOLD;
				if (end_idx > instanceIds.size()){
					end_idx = instanceIds.size();
				}

				List<String> subList = instanceIds.subList(start_idx, end_idx);
				query.setParameter("instanceIds", subList);
				instances.addAll(query.getResultList());
				start_idx = end_idx;
			}

			return instances;
		} else {
			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<InstanceEntity> query = em.createNamedQuery(InstanceEntity.findInstancesByLocation, InstanceEntity.class);
			query.setParameter("cloudScopeId", getUser().getCloudScopeId());
			query.setParameter("locationId", getLocation().getLocationId());
			return query.getResultList();
		}
	}
	
	@Override
	public InstanceEntity getInstance(String instanceId) throws CloudManagerException {
		List<InstanceEntity> instances = getInstances(Arrays.asList(instanceId));
		if (instances.isEmpty())
			throw ErrorCode.CLOUDINSTANCE_NOT_FOUND_PLATFORMINSTANANCE.cloudManagerFault(getCloudScope().getCloudScopeId(),getLocation().getLocationId(),instanceId);
		return instances.get(0);
	}
	
	@Override
	public List<InstanceEntity> removeInstances(final List<String> instanceIds) throws CloudManagerException {
		final List<InstanceEntity> instanceEntitiess = getInstances(instanceIds);
		if (instanceEntitiess.isEmpty())
			return instanceEntitiess;

		// 検知済みのインスタンスの状態が正常か確認。
		final List<String> existedInstanceIds = new ArrayList<>();
		final List<String> platformInstanceIds = new ArrayList<>();
		for (InstanceEntity instanceEntity: instanceEntitiess) {
			if (instanceEntity.getInstanceStatus() != InstanceStatus.terminated &&
				instanceEntity.getInstanceStatus() != InstanceStatus.missing
				) {
				existedInstanceIds.add(instanceEntity.getResourceId());
			}
			platformInstanceIds.add(instanceEntity.getResourceId());
		}
		
		getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IResourceManagement rm = option.getResourceManagement(getLocation(), getUser());
				if (!existedInstanceIds.isEmpty())
					rm.removeInstances(existedInstanceIds);
				
				List<IResourceManagement.Instance> platformInstances = rm.getInstances(platformInstanceIds);
				InstanceUpdater.updator().setNodeDelete(nodeDelete).setNodeRegist(nodeRegist).setInstanceDelete(instanceDelete).setRemoveMissing(true)
						.transactionalUpdateInstanceEntities(getLocation(), getUser(), instanceEntitiess, platformInstances);
			}
		});
		
		for (String instanceId: existedInstanceIds) {
			InstanceMonitorService.getSingleton().startMonitor(
					getCloudScope().getCloudScopeId(),
					getLocation().getLocationId(),
					instanceId,
					Session.current().getContext(),
					InstanceStatus.running, InstanceStatus.suspend, InstanceStatus.terminated, InstanceStatus.stopped);
		}
		return instanceEntitiess;
	}

	@Override
	public InstanceEntity getInstanceByFacilityId(String facilityId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		List<InstanceEntity> result = PersistenceUtil.findByFilter(em, InstanceEntity.class, Filter.apply("facilityId", facilityId));
		if (result.isEmpty())
			throw ErrorCode.CLOUDINSTANCE_NOT_FOUND_BY_FACILITY.cloudManagerFault(facilityId);
		return result.get(0);
	}
	
	@Override
	public List<InstanceEntity> updateInstances(final List<String> instanceIds) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<List<InstanceEntity>>() {
			@Override
			public List<InstanceEntity> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<InstanceEntity> instanceEntities = getInstances(instanceIds);
				if (instanceEntities.isEmpty())
					return Collections.emptyList();
				
				List<String> platformInstanceIds = new ArrayList<>();
				for (InstanceEntity instanceEntity: instanceEntities) {
					platformInstanceIds.add(instanceEntity.getResourceId());
				}
				
				List<IResourceManagement.Instance> instances = getResourceManagement(option).getInstances(platformInstanceIds);
				
				instanceEntities = InstanceUpdater.updator().setNodeDelete(nodeDelete).setNodeRegist(nodeRegist).setInstanceDelete(instanceDelete)
						.transactionalUpdateInstanceEntities(getLocation(), getUser(), instanceEntities, instances);
				
				if (!instances.isEmpty()) {
					IResourceManagement.Location platformLocation = instances.get(0).getLocation();
					// ツリーの更新
					LocationRepositoryUpdater.updateRepositoryNotDeleting(getLocation(), getUser(), platformLocation, null);
				}
				
				// ノードの自動割り当て
				RepositoryUtil.autoAssgineNodesToScope(getCloudScope(), instanceEntities.stream().collect(Collectors.toList()));

				return instanceEntities;
			}
		});
	}
	
	@Override
	public EntityEntity getEntityByPlatformEntityId(String platformEntityId) throws CloudManagerException {
		try {
			TypedQuery<EntityEntity> query = Session.current().getEntityManager().createNamedQuery(EntityEntity.findEntityByPlatformEntityIds, EntityEntity.class);
			query.setParameter("cloudScopeId", getUser().getCloudScopeId());
			query.setParameter("locationId", getLocation().getLocationId());
			query.setParameter("platformEntityIds", Arrays.asList(platformEntityId));
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw ErrorCode.CLOUDENTITY_NOT_FOUND_PLATFORMENTITY.cloudManagerFault(e, getUser().getCloudScopeId(), getLocation().getLocationId(), platformEntityId);
		}
	}

	@Override
	public InstanceEntity modifyInstance(final ModifyInstanceRequest request) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<InstanceEntity>() {
			@Override
			public InstanceEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				final InstanceEntity instanceEntity;
				
				InstanceUpdater updator = InstanceUpdater.updator().setNodeDelete(nodeDelete).setNodeRegist(nodeRegist).setInstanceDelete(instanceDelete);
				
				IResourceManagement.Instance platformInstance;
				try (TransactionScope transaction = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					instanceEntity = getInstance(request.getInstanceId());
					
					IResourceManagement rm = option.getResourceManagement(getLocation(), getUser());
					platformInstance = rm.modifyInstance(request);

					// まずローカルのタグを更新。
					CollectionComparator.compare(instanceEntity.getTags().values(), request.getTags(), new CollectionComparator.Comparator<com.clustercontrol.xcloud.model.ResourceTag, Tag>() {
						public boolean match(com.clustercontrol.xcloud.model.ResourceTag o1, Tag o2) throws CloudManagerException {
							return o1.getTagType() == o2.getTagType() && o1.getKey().equals(o2.getKey());
						}
						public void matched(com.clustercontrol.xcloud.model.ResourceTag o1, Tag o2) throws CloudManagerException {if (o1.getTagType() == TagType.LOCAL) o1.setValue(o2.getValue());}
						public void afterO1(com.clustercontrol.xcloud.model.ResourceTag o1) throws CloudManagerException {
							if (o1.getTagType() != TagType.LOCAL)
								return;
							instanceEntity.getTags().remove(o1.getKey());
						}
						public void afterO2(Tag o2) throws CloudManagerException {
							if (o2.getTagType() != TagType.LOCAL)
								return;
							com.clustercontrol.xcloud.model.ResourceTag tentity = new com.clustercontrol.xcloud.model.ResourceTag();
							tentity.setTagType(o2.getTagType());
							tentity.setKey(o2.getKey());
							tentity.setValue(o2.getValue());
							instanceEntity.getTags().put(tentity.getKey(), tentity);
						}
					});
					
					updator.updateInstanceEntity(getLocation(), instanceEntity, platformInstance);
					
					transaction.complete();
				}
				// ツリーの更新
				LocationRepositoryUpdater.updateRepositoryNotDeleting(getLocation(), getUser(), platformInstance.getLocation(), null);
				
				// ノードの自動割り当て
				RepositoryUtil.autoAssgineNodesToScope(getCloudScope(), Arrays.asList(instanceEntity));
				
				return instanceEntity;
			}
		});
	}

	@Override
	public void rebootInstances(final List<String> instanceIds) throws CloudManagerException {
		getCloudScope().optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getResourceManagement(getLocation(), getUser()).rebootInstances(instanceIds);
				
				for (String instanceId: instanceIds) {
					InstanceMonitorService.getSingleton().startMonitor(
							getCloudScope().getCloudScopeId(),
							getLocation().getLocationId(),
							instanceId,
							Session.current().getContext(),
							InstanceStatus.running, InstanceStatus.suspend, InstanceStatus.terminated, InstanceStatus.stopped);
				}
			}
		});
	}

	@Override
	public InstanceBackupEntity takeInstanceSnapshot(final String instanceId, final String name, final String description, final List<Option> options) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<InstanceBackupEntity>() {
			@Override
			public InstanceBackupEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<InstanceEntity> instanceEntities = getInstances(Arrays.asList(instanceId));
				InstanceEntity instanceEntity;
				if (!instanceEntities.isEmpty()) {
					instanceEntity = instanceEntities.get(0);
				} else {
					throw ErrorCode.CLOUDINSTANCE_NOT_FOUND.cloudManagerFault(instanceId);
				}
				
				final InstanceBackupEntryEntity backupEntry = new InstanceBackupEntryEntity();
				IResourceManagement.InstanceSnapshot snapshot = option.getResourceManagement(getLocation(), getUser()).takeInstanceSnapshot(instanceEntity.getResourceId(), name, description,
					new BackupedDataStore() {
						@Override
						protected Map<String, BackupedData> getBackedupDataMap() {
							return backupEntry.getBackupedData();
						}
					}, options);
				
				backupEntry.setCloudScopeId(Instances.this.getCloudScope().getId());
				backupEntry.setLocationId(Instances.this.getLocation().getLocationId());
				backupEntry.setInstanceId(instanceId);
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
					BackupedData backupedData = new BackupedData();
					backupedData.setName(backup_instanceName);
					backupedData.setValue(instanceEntity.getName());
					backupEntry.getBackupedData().put(backup_instanceName, backupedData);
				}
				{
					BackupedData backupedData = new BackupedData();
					backupedData.setName(backup_memo);
					backupedData.setValue(instanceEntity.getMemo());
					backupEntry.getBackupedData().put(backup_memo, backupedData);
				}
				
				ObjectMapper om = new ObjectMapper();
				ObjectWriter ow = om.writerFor(new TypeReference<List<Tag>>(){});
				try {
					String tagsString = ow.writeValueAsString(Tag.convertWebEntities(new ArrayList<>(instanceEntity.getTags().values())));
					BackupedData backupedData = new BackupedData();
					backupedData.setName(backup_tags);
					backupedData.setValue(tagsString);
					backupEntry.getBackupedData().put(backup_tags, backupedData);
				} catch (JsonProcessingException e) {
					Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
				}
				
				backupEntry.setInstanceBackup(instanceEntity.getBackup());
				
				instanceEntity.getBackup().getEntries().add(backupEntry);
				
				List<InstanceBackupEntryEntity> list  = new ArrayList<>(instanceEntity.getBackup().getEntries());
				Collections.sort(list, new Comparator<InstanceBackupEntryEntity>() {
					@Override
					public int compare(InstanceBackupEntryEntity o1, InstanceBackupEntryEntity o2) {
						return o2.getCreateTime().compareTo(o1.getCreateTime());
					}
				});
				
				int maxcount = HinemosPropertyCommon.xcloud_instance_snapshot_count_max.getIntegerValue();
				final List<InstanceBackupEntryEntity> removed = new ArrayList<>();
				for (int i = maxcount; i < list.size(); ++i) {
					InstanceBackupEntryEntity entry = list.get(i);
					instanceEntity.getBackup().getEntries().remove(entry);
					Session.current().getEntityManager().remove(entry);
					removed.add(entry);
				}
				try {
					option.getResourceManagement(getLocation(), getUser()).deleteInstanceSnapshots(removed);
				} catch(CloudManagerException e) {
					Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
				}
				
				return instanceEntity.getBackup();
			}
		});
	}

	@Override
	public List<InstanceBackupEntity> updateInstanceBackups(final List<String> instanceIds) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<List<InstanceBackupEntity>>() {
			@Override
			public List<InstanceBackupEntity> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<String> queryInstanceIds = instanceIds;
				if (queryInstanceIds == null || queryInstanceIds.isEmpty()) {
					List<InstanceEntity> instances = getInstances(CloudUtil.emptyList(String.class));
					queryInstanceIds = new ArrayList<>();
					for (InstanceEntity instance: instances) {
						queryInstanceIds.add(instance.getResourceId());
					}
				}
				
				if (queryInstanceIds.isEmpty()) {
					return CloudUtil.emptyList(InstanceBackupEntity.class);
				}
				
				int start_idx = 0;
				int end_idx = 0;
				List<InstanceBackupEntity> backups = new ArrayList<>();
				while(start_idx < queryInstanceIds.size()){
					TypedQuery<InstanceBackupEntity> query = Session.current().getEntityManager().createNamedQuery(InstanceBackupEntity.findInstanceBackupsByInstanceIds, InstanceBackupEntity.class);
					query.setParameter("cloudScopeId", getCloudScope().getId());
					query.setParameter("locationId", getLocation().getLocationId());
					end_idx = start_idx + CloudUtil.SQL_PARAM_NUMBER_THRESHOLD;
					if (end_idx > queryInstanceIds.size()){
						end_idx = queryInstanceIds.size();
					}
					
					List<String> subList = queryInstanceIds.subList(start_idx, end_idx);
					query.setParameter("instanceIds", subList);
					backups.addAll(query.getResultList());
					start_idx = end_idx;
				}
				
				Set<InstanceEntity> instanceEntities = new HashSet<>();
				List<InstanceBackupEntryEntity> entries = new ArrayList<>();
				for (InstanceBackupEntity backup: backups) {
					entries.addAll(backup.getEntries());
					instanceEntities.add(backup.getInstance());
				}
				
				List<IResourceManagement.InstanceSnapshot> snapshots = getResourceManagement(option).getInstanceSnapshots(entries);
				for (InstanceBackupEntity backup: backups) {
					CollectionComparator.compare(backup.getEntries(), snapshots, new CollectionComparator.Comparator<InstanceBackupEntryEntity, IResourceManagement.InstanceSnapshot>() {
						@Override
						public boolean match(InstanceBackupEntryEntity o1, InstanceSnapshot o2) throws CloudManagerException {
							return o1.getEntryId().equals(o2.getResourceId());
						}
						@Override
						public void matched(final InstanceBackupEntryEntity o1, InstanceSnapshot o2) throws CloudManagerException {
							o1.setName(o2.getName());
							o1.setDescription(o2.getDescription());
							o1.setStatus(o2.getState());
							o1.setStatusAsPlatform(o2.getStatusAsPlatform());
							if (o2.getCreateTime() != null)
								o1.setCreateTime(o2.getCreateTime());
						}
						@Override
						public void afterO1(InstanceBackupEntryEntity o1) throws CloudManagerException {
							o1.setStatus(InstanceSnapshotStatusType.missing);
							o1.setStatusAsPlatform("");
						}
					});
				}
				
				for (InstanceEntity instanceEntity: instanceEntities) {
					List<InstanceBackupEntryEntity> list  = new ArrayList<>(instanceEntity.getBackup().getEntries());
					Collections.sort(list, new Comparator<InstanceBackupEntryEntity>() {
						@Override
						public int compare(InstanceBackupEntryEntity o1, InstanceBackupEntryEntity o2) {
							return o2.getCreateTime().compareTo(o1.getCreateTime());
						}
					});
					
					int maxcount = HinemosPropertyCommon.xcloud_instance_snapshot_count_max.getIntegerValue();
					final List<InstanceBackupEntryEntity> removed = new ArrayList<>();
					for (int i = maxcount; i < list.size(); ++i) {
						InstanceBackupEntryEntity entry = list.get(i);
						instanceEntity.getBackup().getEntries().remove(entry);
						Session.current().getEntityManager().remove(entry);
						removed.add(entry);
					}
					try {
						IResourceManagement rm = getResourceManagement(option);
						rm.deleteInstanceSnapshots(removed);
					} catch(UnsupportedOperationException e){
					} catch(CloudManagerException e) {
						Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
					}
				}
				return backups;
			}
		});
	}

	@Override
	public List<InstanceBackupEntryEntity> deletInstanceSnapshots(final String instanceId, final List<String> snapshotIds) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<List<InstanceBackupEntryEntity>>() {
			@Override
			public List<InstanceBackupEntryEntity> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<InstanceEntity> instanceEntities = getInstances(Arrays.asList(instanceId));
				
				final InstanceEntity instanceEntity;
				if (!instanceEntities.isEmpty()) {
					instanceEntity = instanceEntities.get(0);
				} else {
					throw ErrorCode.CLOUDINSTANCE_NOT_FOUND.cloudManagerFault(instanceId);
				}

				final List<InstanceBackupEntryEntity> removed = new ArrayList<>();
				CollectionComparator.compare(instanceEntity.getBackup().getEntries(), snapshotIds, new CollectionComparator.Comparator<InstanceBackupEntryEntity, String>() {
					@Override
					public boolean match(InstanceBackupEntryEntity o1, String o2) throws CloudManagerException {
						return o1.getEntryId().equals(o2);
					}
					@Override
					public void matched(InstanceBackupEntryEntity o1, String o2) throws CloudManagerException {
						instanceEntity.getBackup().getEntries().remove(o1);
						Session.current().getEntityManager().remove(o1);
						removed.add(o1);
					}
				});
				
				try {
					IResourceManagement rm = option.getResourceManagement(getLocation(), getUser());
					rm.deleteInstanceSnapshots(removed);
				} catch(CloudManagerException e) {
					Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
				}
				return removed;
			}
		});
	}
	
	@Override
	public InstanceEntity cloneBackupedInstance(final CloneBackupedInstanceRequest request) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<InstanceEntity>() {
			@Override
			public InstanceEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				InstanceEntity newInstanceEntity;
				IResourceManagement.Instance platformInstance;
				try (TransactionScope transaction = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					List<InstanceEntity> instanceEntities = getInstances(Arrays.asList(request.getInstanceId()));
					final InstanceEntity instanceEntity;
					if (!instanceEntities.isEmpty()) {
						instanceEntity = instanceEntities.get(0);
					} else {
						throw ErrorCode.CLOUDINSTANCE_NOT_FOUND.cloudManagerFault(request.getInstanceId());
					}
					
					InstanceBackupEntryEntity backupedEntry = null;
					for (InstanceBackupEntryEntity entry: instanceEntity.getBackup().getEntries()) {
						if (entry.getEntryId().equals(request.getInstanceSnapshotId())) {
							backupedEntry = entry;
							break;
						}
					}
					if (backupedEntry == null)
						throw ErrorCode.CLOUDINSTANCE_NOT_FOUND.cloudManagerFault(request.getInstanceId());
					
					Map<String, String> backupedData = new HashMap<>();
					for (BackupedData entry: backupedEntry.getBackupedData().values()) {
						backupedData.put(entry.getName(), entry.getValue());
					}
					for (BackupedDataEntry entry: request.getModifiedData().getEntries()) {
						backupedData.put(entry.getName(), entry.getValue());
					}
					platformInstance = option.getResourceManagement(getLocation(), getUser()).cloneBackupedInstance(backupedEntry, backupedData, request.getOptions());
					
					ObjectMapper om = new ObjectMapper();
					ObjectReader or = om.readerFor(new TypeReference<List<Tag>>() {});
					
					String name = backupedData.get(backup_instanceName);
					String memo = backupedData.get(backup_memo);
					String tagsString = backupedData.get(backup_tags);
					List<Tag> tags = Collections.emptyList();
					if (tagsString != null)
						tags = or.readValue(tagsString);
					
					newInstanceEntity = InstanceUpdater.updator().setNodeRegist(nodeRegist).setInstanceDelete(instanceDelete).addNewInstanceWithLock(getLocation(), getUser(), name, memo, tags, platformInstance);
					
					transaction.complete();
				} catch (IOException e) {
					throw new CloudManagerException(e);
				}
				
				// ツリーの更新
				LocationRepositoryUpdater.updateRepositoryNotDeleting(getLocation(), getUser(), platformInstance.getLocation(), null);
				
				// ノードの自動割り当て
				RepositoryUtil.autoAssgineNodesToScope(getCloudScope(), Arrays.asList(newInstanceEntity));
				
				return newInstanceEntity;
			}
		});
	}
	
	@Override
	public InstanceEntity assignNode(final String instanceId) throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<InstanceEntity>() {
			@Override
			public InstanceEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				InstanceEntity entity = getInstance(instanceId);
				
				if (entity.getFacilityId() != null)
					throw ErrorCode.ALREADY_ASSIGN_NODE.cloudManagerFault(instanceId, entity.getFacilityId());
				
				IResourceManagement rm = option.getResourceManagement(getLocation(), getUser());
				List<IResourceManagement.Instance> platformInstances = rm.getInstances(Arrays.asList(entity.getResourceId()));
				
				if (platformInstances.isEmpty())
					throw ErrorCode.CLOUDINSTANCE_NOT_FOUND_PLATFORMINSTANANCE.cloudManagerFault(entity.getCloudScopeId(), entity.getLocationId(), instanceId);
				
				InstanceUpdater.updator().setNodeAssine(true).transactionalUpdateInstanceEntities(getLocation(), getUser(), Arrays.asList(entity), platformInstances);
				
				if (!platformInstances.isEmpty()) {
					// ツリーの更新
					LocationRepositoryUpdater.updateRepositoryNotDeleting(getLocation(), getUser(), platformInstances.get(0).getLocation(), null);
					RepositoryUtil.autoAssgineNodesToScope(getCloudScope(), Arrays.asList(entity));
				}
				return entity;
			}
		});
	}

	protected IResourceManagement getResourceManagement(ICloudOption option) {
		if (cacheRm == null) {
			return option.getResourceManagement(getLocation(), getUser());
		} else {
			return cacheRm;
		}
	}

	@Override
	public Instances setCacheResourceManagement(CacheResourceManagement rm) {
		this.cacheRm = rm;
		return this;
	}
}
