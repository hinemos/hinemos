/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.CloudLoginUser;
import com.clustercontrol.xcloud.bean.CloudPlatform;
import com.clustercontrol.xcloud.bean.CloudScope;
import com.clustercontrol.xcloud.bean.Filter;
import com.clustercontrol.xcloud.bean.HCloudScopeRootScope;
import com.clustercontrol.xcloud.bean.HCloudScopeScope;
import com.clustercontrol.xcloud.bean.HEntityNode;
import com.clustercontrol.xcloud.bean.HFolder;
import com.clustercontrol.xcloud.bean.HInstanceNode;
import com.clustercontrol.xcloud.bean.HLocationScope;
import com.clustercontrol.xcloud.bean.HNode;
import com.clustercontrol.xcloud.bean.HRepository;
import com.clustercontrol.xcloud.bean.HScope;
import com.clustercontrol.xcloud.bean.Instance;
import com.clustercontrol.xcloud.bean.InstanceBackup;
import com.clustercontrol.xcloud.bean.Location;
import com.clustercontrol.xcloud.bean.Storage;
import com.clustercontrol.xcloud.bean.StorageBackup;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.FacilityAdditionEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.StorageEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.persistence.Transactional.TransactionOption;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.RepositoryUtil;

@Transactional
public class Repository implements IRepository {
	private static final Logger logger = Logger.getLogger(Repository.class);
	
	public static class RepositoryVisitor {
		public void visitCloudScopeRootScope(FacilityInfo root) throws CloudManagerException {}
		public void visitCloudScopeScope(FacilityInfo parent, FacilityInfo facility, CloudScopeEntity cloudScope) throws CloudManagerException {}
		public void visitLocationScope(FacilityInfo parent, FacilityInfo facility, LocationEntity locationEntity) throws CloudManagerException {}
		public void visitLocationEntity(LocationEntity locationEntity) throws CloudManagerException {}
		public void visitFolder(FacilityInfo parent, FacilityInfo facility) throws CloudManagerException {}
		public void visitScope(FacilityInfo parent, FacilityInfo facility) throws CloudManagerException {}
		public void visitNode(FacilityInfo parent, FacilityInfo facility) throws CloudManagerException {}
		public void visitInstance(FacilityInfo parent, FacilityInfo facility, InstanceEntity instanceEntity) throws CloudManagerException {}
		public void visitEntity(FacilityInfo parent, FacilityInfo facility, EntityEntity entityEntity) throws CloudManagerException {}
		public void visitStart() throws CloudManagerException {}
		public void visitEnd() throws CloudManagerException {}
		public EntityEntity findEntityEntity(String facilityId) {return null;}
		public InstanceEntity findInstanceEntity(String facilityId) {return null;}
	}
	
	public class HScopeRepositoryVisitor extends RepositoryVisitor {
		// リポジトリ階層構築のための利用する
		private HRepository hRepository = new HRepository();
		
		private CloudScopeEntity cloudScope;

		// 作成または更新済みの HNode のキャッシュ。
		private Map<String, HScope> scopeMap = new HashMap<>();
		private Map<String, HNode> nodeMap = new HashMap<>();
		private Map<String, Instance> instanceMap = new HashMap<>();

		// InstanceEntity,EntityEntityのキャッシュ(検索用にファシリティIDをキーにする)
		private Map<String,InstanceEntity> instanceEntityMap = new HashMap<>();
		private Map<String,EntityEntity> entityEntityMap = new HashMap<>();

		@Override
		public void visitCloudScopeRootScope(FacilityInfo root) throws CloudManagerException {
			HCloudScopeRootScope hRoot = new HCloudScopeRootScope();
			hRoot.setId(root.getFacilityId());
			hRoot.setName(root.getFacilityName());
			hRepository.getScopes().add(hRoot);
			hRepository.getFacilities().add(hRoot);
			scopeMap.put(hRoot.getId(), hRoot);
		}
		@Override
		public void visitCloudScopeScope(FacilityInfo parent, FacilityInfo facility, CloudScopeEntity cloudScope) throws CloudManagerException {
			logger.debug("visitCloudScopeScope(): start. CloudScopeID: " + cloudScope.getCloudScopeId());

			this.cloudScope = cloudScope;
			
			HCloudScopeScope hCloudScope = new HCloudScopeScope();
			hCloudScope.setId(FacilityIdUtil.getCloudScopeScopeId(cloudScope));
			hCloudScope.setName(cloudScope.getName());
			
			CloudScope webCloudScope = CloudScope.convertWebEntity(cloudScope);
			hCloudScope.setCloudScope(webCloudScope);
			hRepository.getCloudScopes().add(webCloudScope);
			
			hRepository.getLoginUsers().addAll(CloudLoginUser.convertWebEntities(CloudManager.singleton().getLoginUsers().getCloudLoginUserByCloudScope(cloudScope.getCloudScopeId())));
			
			if (cloudScope.getLocations().size() == 1) {
				hCloudScope.setLocation(Location.convertWebEntity(cloudScope.getLocations().get(0)));
				
				HinemosEntityManager em = Session.current().getEntityManager();
				long startGetInstances = HinemosTime.currentTimeMillis();
				List<InstanceEntity> instances = PersistenceUtil.findByFilter(em, InstanceEntity.class, Filter.apply("cloudScopeId", cloudScope.getCloudScopeId()), Filter.apply("locationId", hCloudScope.getLocation().getId()));
				for (InstanceEntity instanceEntity: instances) {
					Instance instance = Instance.convertWebEntity(instanceEntity);
					instanceMap.put(instanceEntity.getResourceId(), instance);
					hRepository.getInstances().add(instance);
					hRepository.getInstanceBackups().add(InstanceBackup.convertWebEntity(instanceEntity.getBackup()));
					// InstanceEntityをキャッシュ
					instanceEntityMap.put(instanceEntity.getFacilityId(), instanceEntity);
				}

				logger.debug("visitCloudScopeScope(): End InstanceQuery. " + (HinemosTime.currentTimeMillis() - startGetInstances) + " ms.");
				long startGetStorages = HinemosTime.currentTimeMillis();
				List<StorageEntity> storages = PersistenceUtil.findByFilter(em, StorageEntity.class, Filter.apply("cloudScopeId", cloudScope.getCloudScopeId()), Filter.apply("locationId", hCloudScope.getLocation().getId()));
				for (StorageEntity storageEntity: storages) {
					hRepository.getStorages().add(Storage.convertWebEntity(storageEntity));
					hRepository.getStorageBackups().add(StorageBackup.convertWebEntity(storageEntity.getBackup()));
				}
				logger.debug("visitCloudScopeScope(): End StorageEntityQuery. " + (HinemosTime.currentTimeMillis() - startGetStorages) + " ms.");
			}
			
			HScope scope = scopeMap.get(parent.getFacilityId());
			if (scope == null)
				throw new InternalManagerError();
			
			hRepository.getScopes().add(hCloudScope);
			scope.addScope(hCloudScope);
			scopeMap.put(hCloudScope.getId(), hCloudScope);
		}
		@Override
		public void visitLocationScope(FacilityInfo parent, FacilityInfo facility, LocationEntity locationEntity) throws CloudManagerException {				// ロケーションをクライアントへ返す形式へ変換。
			HLocationScope hLocation = new HLocationScope();
			hLocation.setId(FacilityIdUtil.getLocationScopeId(locationEntity.getCloudScope().getId(), locationEntity));
			hLocation.setName(locationEntity.getName());
			
			hLocation.setLocation(Location.convertWebEntity(locationEntity));

			long startGetInstances = HinemosTime.currentTimeMillis();
			HinemosEntityManager em = Session.current().getEntityManager();
			List<InstanceEntity> instances = PersistenceUtil.findByFilter(em, InstanceEntity.class, Filter.apply("cloudScopeId", cloudScope.getCloudScopeId()), Filter.apply("locationId", hLocation.getLocation().getId()));
			for (InstanceEntity instanceEntity: instances) {
				Instance instance = Instance.convertWebEntity(instanceEntity);
				instanceMap.put(instanceEntity.getResourceId(), instance);
				hRepository.getInstances().add(instance);
				hRepository.getInstanceBackups().add(InstanceBackup.convertWebEntity(instanceEntity.getBackup()));
				// InstanceEntityをキャッシュ
				instanceEntityMap.put(instanceEntity.getFacilityId(), instanceEntity);
			}
			logger.debug("visitLocationScope(): End InstanceQuery. " + (HinemosTime.currentTimeMillis() - startGetInstances) + " ms.");

			long startGetStorages = HinemosTime.currentTimeMillis();
			List<StorageEntity> storages = PersistenceUtil.findByFilter(em, StorageEntity.class, Filter.apply("cloudScopeId", cloudScope.getCloudScopeId()), Filter.apply("locationId", hLocation.getLocation().getId()));
			for (StorageEntity storageEntity: storages) {
				hRepository.getStorages().add(Storage.convertWebEntity(storageEntity));
				hRepository.getStorageBackups().add(StorageBackup.convertWebEntity(storageEntity.getBackup()));
			}
			logger.debug("visitLocationScope(): End StorageEntityQuery. " + (HinemosTime.currentTimeMillis() - startGetStorages) + " ms.");

			HScope scope = scopeMap.get(parent.getFacilityId());
			if (scope == null)
				throw new InternalManagerError();

			hRepository.getScopes().add(hLocation);
			scope.addScope(hLocation);
			scopeMap.put(hLocation.getId(), hLocation);
		}
		@Override
		public void visitLocationEntity(LocationEntity locationEntity) throws CloudManagerException {
			HinemosEntityManager em = Session.current().getEntityManager();
			// EntityEntityをキャッシュする。
			long startGetEntities = HinemosTime.currentTimeMillis();
			List<EntityEntity> entities = PersistenceUtil.findByFilter(em, EntityEntity.class, Filter.apply("cloudScopeId", cloudScope.getCloudScopeId()), Filter.apply("locationId", locationEntity.getLocationId()));
			for (EntityEntity entityEntity : entities) {
				entityEntityMap.put(entityEntity.getFacilityId(), entityEntity);
			}
			logger.debug("visitLocationEntity(): End EntityEntityQuery. " + (HinemosTime.currentTimeMillis() - startGetEntities) + " ms.");
		};
		@Override
		public void visitFolder(FacilityInfo parent, FacilityInfo facility) throws CloudManagerException {
			Pattern p = Pattern.compile(String.format("^_(.*)(_%s|_%s_(.*))$", cloudScope.getId(), cloudScope.getId()));
			Matcher m = p.matcher(facility.getFacilityId());
			if (!m.matches())
				throw new InternalManagerError();
			
			HFolder hFolder = new HFolder();
			hFolder.setId(facility.getFacilityId());
			hFolder.setName(facility.getFacilityName());
			hFolder.setFolderType(m.group(1));
			
			HScope scope = scopeMap.get(parent.getFacilityId());
			if (scope == null)
				throw new InternalManagerError();

			hRepository.getScopes().add(hFolder);
			scope.addScope(hFolder);
			scopeMap.put(hFolder.getId(), hFolder);
		}
		@Override
		public void visitScope(FacilityInfo parent, FacilityInfo facility) throws CloudManagerException {
		}
		@Override
		public void visitInstance(FacilityInfo parent, FacilityInfo facility, InstanceEntity instanceEntity) throws CloudManagerException {
			HScope scope = scopeMap.get(parent.getFacilityId());
			if (scope == null)
				throw new InternalManagerError();

			HNode hNode = nodeMap.get(instanceEntity.getFacilityId());
			if (hNode == null) {
				HInstanceNode instanceNode = new HInstanceNode();
				instanceNode.setId(instanceEntity.getFacilityId());
				instanceNode.setName(facility.getFacilityName());
				instanceNode.setInstance(instanceMap.get(instanceEntity.getResourceId()));
				
				hRepository.getNodes().add(instanceNode);
				nodeMap.put(instanceEntity.getFacilityId(), instanceNode);
				hNode = instanceNode;
			}
			scope.addNode(hNode);
		}
		@Override
		public void visitEntity(FacilityInfo parent, FacilityInfo facility, EntityEntity entityEntity) throws CloudManagerException {
			HScope scope = scopeMap.get(parent.getFacilityId());
			if (scope == null)
				throw new InternalManagerError();

			HNode hNode = nodeMap.get(entityEntity.getFacilityId());
			if (hNode == null) {
				HEntityNode entityNode = new HEntityNode();
				entityNode.setId(entityEntity.getFacilityId());
				entityNode.setName(entityEntity.getName());
				entityNode.setEntityType(entityEntity.getEntityType());
				
				hRepository.getNodes().add(entityNode);
				nodeMap.put(entityEntity.getFacilityId(), entityNode);
				hNode = entityNode;
			}
			scope.addNode(hNode);
		}
		@Override
		public void visitNode(FacilityInfo parent, FacilityInfo facility) throws CloudManagerException {
		}
		@Override
		public void visitStart() throws CloudManagerException {
			hRepository.getPlatforms().addAll(CloudPlatform.convertWebEntities(CloudManager.singleton().getPlatforms().getAllCloudPlatforms()));
		}
		@Override
		public void visitEnd() throws CloudManagerException {
			// フォルダー情報更新
			HinemosEntityManager em = Session.current().getEntityManager();
			List<FacilityAdditionEntity> entities = PersistenceUtil.findAll(em, FacilityAdditionEntity.class);
			
			CollectionComparator.compare(scopeMap.values(), entities, new CollectionComparator.Comparator<HScope, FacilityAdditionEntity>() {
				@Override
				public boolean match(HScope o1, FacilityAdditionEntity o2) throws CloudManagerException {
					return o1.getId().equals(o2.getFacilityId());
				}
				@Override
				public void matched(final HScope entity, FacilityAdditionEntity o2) throws CloudManagerException {
					if (entity instanceof HFolder)
						((HFolder)entity).setFolderType(o2.getType());
					
					for (com.clustercontrol.xcloud.model.ExtendedProperty mep: o2.getExtendedProperties().values()) {
						com.clustercontrol.xcloud.bean.ExtendedProperty ep = new com.clustercontrol.xcloud.bean.ExtendedProperty();
						ep.setName(mep.getName());
						ep.setValue(mep.getValue());
						entity.getExtendedProperties().add(ep);
					}
				}
			});
		}

		/**
		 * キャッシュからEntityEntityを検索する。一致するものがなければnullを返す。
		 */
		@Override
		public EntityEntity findEntityEntity(String facilityId) {
			return entityEntityMap.get(facilityId);
		}

		/**
		 * キャッシュからInstanceEntityを検索する。一致するものがなければnullを返す。
		 */
		@Override
		public InstanceEntity findInstanceEntity(String facilityId) {
			return instanceEntityMap.get(facilityId);
		}

		public HRepository getRepository() {
			return hRepository;
		}
	};

	@Override
	public HRepository getRepository() throws CloudManagerException {
		HScopeRepositoryVisitor visitor = new HScopeRepositoryVisitor();
		CloudRepositoryWalker.walkCloudRepository(visitor);
		return visitor.getRepository();
	}
	
	@Override
	public HRepository updateLocationRepository(String cloudScopeId, String locationId) throws CloudManagerException {
		logger.debug(String.format("updateLocationRepository() start cloudScopeId=%s, locationId=%s",
				cloudScopeId
				, locationId));
		HRepository hRepository = updateLocationRepository(cloudScopeId, locationId, null);
		logger.debug(String.format("updateLocationRepository() end cloudScopeId=%s, locationId=%s",
				cloudScopeId
				, locationId));
		return hRepository;
	}

	@Override
	public HRepository updateLocationRepository(String cloudScopeId, String locationId, String hinemosUser) throws CloudManagerException {
		// ロケーションまでのスコープを更新
		final CloudScopeEntity cloudScope;
		if (hinemosUser == null) {
			cloudScope = CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId);
		} else {
			cloudScope = CloudManager.singleton().getCloudScopes().getCloudScopeByHinemosUser(cloudScopeId, hinemosUser);
		}
		
		final CloudLoginUserEntity user;
		if (hinemosUser == null) {
			user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScope.getId());
		} else {
			user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUser(cloudScope.getCloudScopeId(), hinemosUser);
		}
		
		return user.getCloudScope().optionCall(new CloudScopeEntity.OptionCallable<HRepository>() {
			@Override
			public HRepository call(CloudScopeEntity cloudScope, ICloudOption option) throws CloudManagerException {
				logger.debug(String.format("updateLocationRepository() call start cloudScopeId=%s, locationId=%s",
						cloudScopeId
						, locationId));
	
				LocationEntity location = cloudScope.getLocation(locationId);
				
				// クラウド側からリソースツリーを取得。
				IResourceManagement.Location platformLocation = getResourceManagement(option, location, user).getResourceHierarchy();
				
				List<InstanceEntity> instanceEntities;
				List<EntityEntity> entityEntities;
				
				ILock lock = CloudUtil.getLock(Repository.class.getName(), cloudScope.getId(), location.getLocationId());
				logger.debug(String.format("updateLocationRepository() getLock cloudScopeId=%s, locationId=%s",
						cloudScope.getId()
						, location.getLocationId()));
				try {
					// クラウドスコープID単位で書き込みロックを取得
					lock.writeLock();
					logger.debug(String.format("updateLocationRepository() writeLock cloudScopeId=%s, locationId=%s",
							cloudScope.getId()
							, location.getLocationId()));
					
					try (TransactionScope scope = new TransactionScope(TransactionOption.RequiredNew)) {						
						// リソースの更新
						instanceEntities = InstanceUpdater.updator().setCacheResourceManagement(Repository.this.cacheRm).transactionalUpdateInstanceEntities(
								location, user,
								PersistenceUtil.findByFilter(Session.current().getEntityManager(), InstanceEntity.class, Filter.apply("cloudScopeId", cloudScope.getId()), Filter.apply("locationId", location.getLocationId())),
								platformLocation.getInstances());
						
						entityEntities = EntityUpdater.updator().setCacheResourceManagement(Repository.this.cacheRm).transactionalUpdateEntityEntities(
								location,
								PersistenceUtil.findByFilter(Session.current().getEntityManager(), EntityEntity.class, Filter.apply("cloudScopeId", cloudScope.getId()), Filter.apply("locationId", location.getLocationId())),
								platformLocation.getEntities());
						scope.complete();
					} 
					
					// ツリーの更新
					HScopeRepositoryVisitor visitor = new HScopeRepositoryVisitor();
					LocationRepositoryUpdater.updateRepository(location, user, platformLocation, visitor);
					
					RepositoryUtil.autoAssgineNodesToScope(cloudScope, instanceEntities.stream().collect(Collectors.toList()));
					RepositoryUtil.autoAssgineNodesToScope(cloudScope, entityEntities.stream().collect(Collectors.toList()));
					logger.debug(String.format("updateLocationRepository() call end cloudScopeId=%s, locationId=%s",
							cloudScopeId
							, locationId));
					return visitor.getRepository();

				} finally {
					lock.writeUnlock();
					logger.debug(String.format("updateLocationRepository() writeUnlock cloudScopeId=%s, locationId=%s",
							cloudScope.getId()
							, location.getLocationId()));
				}
			}
		});
	}

	@Override
	public void createCloudScopeRepository(CloudScopeEntity cloudScope) throws CloudManagerException {
		RepositoryUtil.createCloudScopeRepository(cloudScope);
	}

	@Override
	public void removeCloudScopeRepository(CloudScopeEntity cloudScope) throws CloudManagerException {
		RepositoryUtil.removeCloudScopeRepository(cloudScope);
	}
	
	private CacheResourceManagement cacheRm;
	
	protected IResourceManagement getResourceManagement(ICloudOption option, LocationEntity location, CloudLoginUserEntity user) {
		if (cacheRm == null) {
			return option.getResourceManagement(location, user);
		} else {
			return cacheRm;
		}
	}
	
	@Override
	public IRepository setCacheResourceManagement(CacheResourceManagement rm) {
		this.cacheRm = rm;
		return this;
	}
	
	@Override
	public HRepository getRepository(String ownerRoleId) throws CloudManagerException {
		HScopeRepositoryVisitor visitor = new HScopeRepositoryVisitor();
		CloudRepositoryWalker.walkCloudRepositoryByOwnerRole(visitor, ownerRoleId);
		return visitor.getRepository();
	}
}
