/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.Tag;
import com.clustercontrol.xcloud.bean.TagType;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.DataType;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.ExtendedProperty;
import com.clustercontrol.xcloud.model.FacilityAdditionEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.LocationResourceEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.CloudMessageUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class EntityUpdater {
	public static abstract class NodeDeviceInfoMixin {
		@JsonIdentityReference(alwaysAsId = true)
		public abstract NodeInfo getNodeEntity();
	}
	
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property = "facilityId")
	public static abstract class NodeInfoMixin {
	}
	
	private static final Logger logger = Logger.getLogger(EntityUpdater.class);
	
	private boolean autoNodeRegist = HinemosPropertyCommon.xcloud_autoregist_node_entity.getBooleanValue();
	private boolean autoNodeDelete = HinemosPropertyCommon.xcloud_autodelete_node_entity.getBooleanValue();
	
	private EntityUpdater() {
	}
	
	public EntityUpdater setAutoNodeRegist(boolean autoRegist) {
		this.autoNodeRegist = autoRegist;
		return this;
	}
	
	public EntityEntity updateEntityEntity(LocationEntity location, EntityEntity entityEntity, IResourceManagement.Entity platformEntity) throws CloudManagerException {
		// 登録済みの情報を更新。
		updateWithPlatform(entityEntity, platformEntity);
		
		if (entityEntity.getFacilityId() != null) {
			try {
				RepositoryControllerBean repositoryControllerBean = RepositoryControllerBeanWrapper.bean();
				NodeInfo origin = repositoryControllerBean.getNodeFull(entityEntity.getFacilityId());
				StringBuilder changeLog = new StringBuilder();
				String oldValue, newValue;

				NodeInfo nodeInfo = origin.clone();
				
				//nodeInfo.setSubPlatformFamily(entityEntity.getCloudScope().getPlatformId());
				oldValue = nodeInfo.getSubPlatformFamily();
				newValue = entityEntity.getCloudScope().getPlatformId();
				if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
					nodeInfo.setSubPlatformFamily(newValue);
					changeLog.append("SubPlatformFamily:").append(oldValue).append("->").append(newValue).append(";");
				}

				//nodeInfo.setCloudService(entityEntity.getCloudScope().getPlatformId());
				oldValue = nodeInfo.getCloudService();
				newValue = entityEntity.getCloudScope().getPlatformId();
				if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
					nodeInfo.setCloudService(newValue);
					changeLog.append("CloudService:").append(oldValue).append("->").append(newValue).append(";");
				}
				//nodeInfo.setCloudScope(entityEntity.getCloudScope().getCloudScopeId());
				oldValue = nodeInfo.getCloudScope();
				newValue = entityEntity.getCloudScope().getCloudScopeId();
				if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
					nodeInfo.setCloudScope(newValue);
					changeLog.append("CloudScope:").append(oldValue).append("->").append(newValue).append(";");
				}
				//nodeInfo.setCloudResourceName(entityEntity.getName());
				oldValue = nodeInfo.getCloudResourceName();
				// DBの桁数に合わせてカットする
				newValue = CloudUtil.truncateString(entityEntity.getName(), com.clustercontrol.repository.util.RepositoryUtil.NODE_CLOUD_RESOURCE_NAME_MAX_BYTE);
				if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
					nodeInfo.setCloudResourceName(newValue);
					changeLog.append("CloudResourceName:").append(oldValue).append("->").append(newValue).append(";");
				}
				//nodeInfo.setCloudResourceType(platformEntity.getResourceTypeAsPlatform());
				oldValue = nodeInfo.getCloudResourceType();
				newValue = platformEntity.getResourceTypeAsPlatform();
				if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
					nodeInfo.setCloudResourceType(newValue);
					changeLog.append("CloudResourceType:").append(oldValue).append("->").append(newValue).append(";");
				}
				//nodeInfo.setCloudResourceId(entityEntity.getPlatformEntityId());
				oldValue = nodeInfo.getCloudResourceId();
				newValue = entityEntity.getPlatformEntityId();
				if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
					nodeInfo.setCloudResourceId(newValue);
					changeLog.append("CloudResourceId:").append(oldValue).append("->").append(newValue).append(";");
				}
				//nodeInfo.setCloudLocation(entityEntity.getLocationId());
				oldValue = nodeInfo.getCloudLocation();
				newValue = entityEntity.getLocationId();
				if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
					nodeInfo.setCloudLocation(newValue);
					changeLog.append("CloudLocation:").append(oldValue).append("->").append(newValue).append(";");
				}

				if (platformEntity.canDecorate())
					platformEntity.decorate(entityEntity, nodeInfo);

				if(0<changeLog.length()){
					logger.info(String.format("Update node. autoRegist=%b,FacilityID=%s,InstanceId=%s,log=%s", ActionMode.isAutoDetection(), nodeInfo.getFacilityId(), entityEntity.getResourceId(),changeLog));
	
					ObjectMapper om = new ObjectMapper();
					om.addMixIn(NodeDeviceInfo.class, NodeDeviceInfoMixin.class);
					om.addMixIn(NodeInfo.class, NodeInfoMixin.class);

					ObjectWriter ow = om.writer();
					try {
						String modified = ow.writeValueAsString(nodeInfo);
						String copied = ow.writeValueAsString(origin.clone());
						if (!modified.equals(copied)) {
							try (ModifiedEventNotifier<NodeInfo> notifier = new ModifiedEventNotifier<>(NodeInfo.class, CloudConstants.Node_Instance, nodeInfo)) {
								try {
									repositoryControllerBean.modifyNode(nodeInfo);
									notifier.setCompleted();
								} catch (InvalidSetting | HinemosUnknown e) {
									throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
								}
							}
						}
					} catch (JsonProcessingException e) {
						logger.warn(e.getMessage(), e);
					}
				}
			} catch (FacilityNotFound | HinemosUnknown | InvalidRole e) {
				throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
			}
			
			// タグは、クラウドとの比較なので、platformInstance のタグには、タグ種別がクラウドか自動しか入っていないという前提。
			CollectionComparator.compare(entityEntity.getTags().values(), platformEntity.getTags(), new CollectionComparator.Comparator<com.clustercontrol.xcloud.model.ResourceTag, Tag>() {
				public boolean match(com.clustercontrol.xcloud.model.ResourceTag o1, Tag o2) throws CloudManagerException {
					return o1.getTagType() == o2.getTagType() && o1.getKey().equals(o2.getKey());
				}
				public void matched(com.clustercontrol.xcloud.model.ResourceTag o1, Tag o2) throws CloudManagerException {if (o1.getTagType() != TagType.LOCAL) o1.setValue(o2.getValue());}
				public void afterO1(com.clustercontrol.xcloud.model.ResourceTag o1) throws CloudManagerException {
					if (o1.getTagType() == TagType.LOCAL)
						return;
					entityEntity.getTags().remove(o1.getKey());
				}
				public void afterO2(Tag o2) throws CloudManagerException {
					if (o2.getTagType() == TagType.LOCAL)
						return;
					com.clustercontrol.xcloud.model.ResourceTag tentity = new com.clustercontrol.xcloud.model.ResourceTag();
					tentity.setTagType(o2.getTagType());
					tentity.setKey(o2.getKey());
					tentity.setValue(o2.getValue());
					entityEntity.getTags().put(tentity.getKey(), tentity);
				}
			});
		} else {
			if (autoNodeRegist)
				addHinemosNode(location, entityEntity, platformEntity);
		}
		
		return entityEntity;
	}
				
	public void disableEntityEntity(EntityEntity entityEntity) throws CloudManagerException {
		CloudScopeEntity cloudScope = Session.current().get(CloudScopeEntity.class);

		// ノードと紐づいていないなら、削除。
		HinemosEntityManager em = Session.current().getEntityManager();
		em.remove(entityEntity);

		// AWS インスタンスがないので自動削除。
		if (entityEntity.getFacilityId() != null) {
			try {
				if (autoNodeDelete) {
					if (ActionMode.isAutoDetection())
						logger.info(String.format("Delete Node, Method=autoRegist, FacilityID=%s, EntityId=%s", entityEntity.getFacilityId(), entityEntity.getResourceId()));	
					
					RepositoryControllerBeanWrapper.bean().deleteNode(new String[]{entityEntity.getFacilityId()});
				} else {
					TypedQuery<FacilityAdditionEntity> query = em.createNamedQuery(FacilityAdditionEntity.findParentFacilityAdditionsOfFacility, FacilityAdditionEntity.class);
					query.setParameter("facilityId", entityEntity.getFacilityId());
					
					List<FacilityAdditionEntity> additions;
					try {
						additions = query.getResultList();
					} catch(NoResultException e) {
						return;
					}
					
					for (FacilityAdditionEntity addition: additions) {
						ExtendedProperty ep = addition.getExtendedProperties().get(CloudConstants.EPROP_CloudScope);
						if (ep == null || !ep.getValue().equals(cloudScope.getId()))
							continue;
						
						RepositoryControllerBeanWrapper.bean().releaseNodeScope(addition.getFacilityId(), new String[]{entityEntity.getFacilityId()});
					}
				}
			} catch (Exception e) {
				throw ErrorCode.AUTOUPDATE_NOT_DELETE_FACILITY.cloudManagerFault(e, entityEntity.getCloudScopeId(), entityEntity.getLocationId(), entityEntity.getFacilityId(), CloudManagerException.getMessage(e));
			}
		}
	}
	
	protected EntityEntity convertPlatformEntity(LocationEntity location, IResourceManagement.Entity platformEntity) {
		HinemosEntityManager em = Session.current().getEntityManager();
		
		CloudScopeEntity cloudScope = Session.current().get(CloudScopeEntity.class);
		
		// DB に追加する情報を作成。
		EntityEntity entityEntity = new EntityEntity();
		entityEntity.setCloudScopeId(cloudScope.getCloudScopeId());
		entityEntity.setLocationId(location.getLocationId());
		entityEntity.setResourceId(FacilityIdUtil.nextId());
		entityEntity.setPlatformEntityId(platformEntity.getResourceId());
		// DBの桁数に合わせてカットする
		entityEntity.setName(CloudUtil.truncateString(platformEntity.getName(), CloudUtil.ENTITY_NAME_MAX_BYTE));
		entityEntity.setEntityType(platformEntity.getResourceTypeAsPlatform());
		entityEntity.setCloudScope(cloudScope);
		
		while (true) {
			FacilityInfo f = em.find(FacilityInfo.class, FacilityIdUtil.getResourceId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId(), entityEntity.getResourceId()), ObjectPrivilegeMode.READ);
			if (f == null)
				break;
			entityEntity.setResourceId(FacilityIdUtil.nextId());
		}
		
		try {
			PersistenceUtil.persist(em, entityEntity);
		} catch (EntityExistsException e) {
			// ユーザーが実施するインスタンス作成および自動検知によるインスタンス作成の同期をとり、
			// インスタンス ID を自動採番しているので、この例外が起きた場合は不具合
			throw new InternalManagerError(e.getMessage(), e);
		}

		return entityEntity;
	}
	
	public EntityEntity addExistedEntity(LocationEntity location, IResourceManagement.Entity platformEntity) throws CloudManagerException {
		EntityEntity entityEntity = convertPlatformEntity(location, platformEntity);
		
		if (autoNodeRegist)
			addHinemosNode(location, entityEntity, platformEntity);
		
		return entityEntity;
	}
	
	protected NodeInfo addHinemosNode(LocationEntity location, EntityEntity entityEntity, IResourceManagement.Entity platformEntity) throws CloudManagerException {
		CloudScopeEntity cloudScope = Session.current().get(CloudScopeEntity.class);

		String facilityId = FacilityIdUtil.getEntityId(cloudScope.getCloudScopeId(), platformEntity.getResourceTypeAsPlatform(), entityEntity.getResourceId());
		String entityName = entityEntity.getName() == null || entityEntity.getName().isEmpty() ? entityEntity.getPlatformEntityId(): entityEntity.getName();

		NodeInfo nodeInfo = CloudUtil.createNodeInfo(
				facilityId,
				entityName,
				"OTHER", // Hard-code
				cloudScope.getPlatformId(),
				entityName,
				"Hinemos Auto Registered",
				cloudScope.getPlatformId(),
				cloudScope.getId(),
				platformEntity.getName(),
				platformEntity.getResourceTypeAsPlatform(),
				entityEntity.getPlatformEntityId(),
				location.getLocationId(),
				cloudScope.getOwnerRoleId());
		// Entity do not need to be search
		nodeInfo.setAutoDeviceSearch(Boolean.FALSE);

		if (platformEntity.canDecorate())
			platformEntity.decorate(entityEntity, nodeInfo);
		
		logger.info(String.format("Add node. autoRegist=%b,FacilityID=%s,EntityId=%s", ActionMode.isAutoDetection(), nodeInfo.getFacilityId(), entityEntity.getResourceId()));
		
		try {
			RepositoryControllerBeanWrapper.bean().addNode(nodeInfo);
			Session.current().getEntityManager().flush();
			
			FacilityAdditionEntity fa = new FacilityAdditionEntity();
			fa.setFacilityId(nodeInfo.getFacilityId());
			fa.setCloudScopeId(cloudScope.getCloudScopeId());
			fa.setType(entityEntity.getResourceType());
			ExtendedProperty ep = new ExtendedProperty();
			ep.setName(CloudConstants.EPROP_Entity);
			ep.setDataType(DataType.string);
			ep.setValue(entityEntity.getResourceId());
			fa.getExtendedProperties().put(CloudConstants.EPROP_Entity, ep);
			PersistenceUtil.persist(Session.current().getEntityManager(), fa);

		} catch (FacilityDuplicate e) {
			// ユーザーが実施するインスタンス作成および自動検知によるインスタンス作成の同期をとり、
			// インスタンス ID を自動採番していることから、ファシリティ ID が重複するはずがないので、この例外が起きた場合は不具合
			throw new InternalManagerError(e.getMessage(), e);
		} catch (InvalidSetting | HinemosUnknown e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}

		entityEntity.setFacilityId(nodeInfo.getFacilityId());
		
		return nodeInfo;
	}
	
	protected void updateWithPlatform(EntityEntity entityEntity, IResourceManagement.Entity platformEntity) throws CloudManagerException {
		String entityName = null;
		if (platformEntity.getName() == null) {
			entityName = platformEntity.getResourceId();
		} else {
			entityName = platformEntity.getName();
		}
		// DBの桁数に合わせてカットする
		entityEntity.setName(CloudUtil.truncateString(entityName, CloudUtil.ENTITY_NAME_MAX_BYTE));
	}
	
	public List<EntityEntity> transactionalUpdateEntityEntities(LocationEntity location, List<EntityEntity> entityEntities, List<IResourceManagement.Entity> platformEntities) throws CloudManagerException {
		CloudScopeEntity cloudScope = Session.current().get(CloudScopeEntity.class);
		
		final List<LocationResourceEntity.LocationResourceEntityPK> updateds = new ArrayList<>();
		final Map<String, Map<String, com.clustercontrol.xcloud.model.ResourceTag>> updatedTags = new HashMap<>();
		CollectionComparator.compare(entityEntities, platformEntities, new CollectionComparator.Comparator<EntityEntity, IResourceManagement.Entity>() {
			@Override
			public boolean match(EntityEntity o1, IResourceManagement.Entity o2) throws CloudManagerException {
				return o1.getPlatformEntityId().equals(o2.getResourceId());
			}
			@Override
			public void matched(EntityEntity o1, IResourceManagement.Entity o2) throws CloudManagerException {
				try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					HinemosEntityManager em = Session.current().getEntityManager();

					//　EntityManager が切り替わっているので、再度取得。
					EntityEntity entity = em.find(EntityEntity.class, o1.getId(), ObjectPrivilegeMode.READ);
					
					EntityEntity updated = updateEntityEntity(location, entity, o2);
					if (updated != null) {
						updateds.add(updated.getId());
						updatedTags.put(updated.getFacilityId(), updated.getTags());
					}
					
					scope.complete();
				} catch (CloudManagerException e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(cloudScope.getCloudScopeId(), o2.getResourceId(), e);
				} catch (Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(cloudScope.getCloudScopeId(), o2.getResourceId(), e);
				}
			}
			@Override
			public void afterO1(EntityEntity o1) throws CloudManagerException {
				try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					HinemosEntityManager em = Session.current().getEntityManager();

					//　EntityManager が切り替わっているので、再度取得。
					EntityEntity entity = em.find(EntityEntity.class, o1.getId(), ObjectPrivilegeMode.READ);
					if (entity != null) {
						disableEntityEntity(entity);
					}
					scope.complete();
				} catch (CloudManagerException e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(cloudScope.getCloudScopeId(), o1.getPlatformEntityId(), e);
				} catch (Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(cloudScope.getCloudScopeId(), o1.getPlatformEntityId(), e);
				}
			}
			@Override
			public void afterO2(IResourceManagement.Entity o2) throws CloudManagerException {
				try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					EntityEntity updated = addExistedEntity(location, o2);
					if (updated != null) {
						updateds.add(updated.getId());
						updatedTags.put(updated.getFacilityId(), updated.getTags());
					}
					
					scope.complete();
				} catch (CloudManagerException e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(cloudScope.getCloudScopeId(), o2.getResourceId(), e);
				} catch (Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(cloudScope.getCloudScopeId(), o2.getResourceId(), e);
				}
			}
		});
		
		// メモリ肥大化対応。
		List<EntityEntity> updatedEntities = new ArrayList<>();
		try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
			HinemosEntityManager em = Session.current().getEntityManager();
			
			for (LocationResourceEntity.LocationResourceEntityPK pk: updateds) {
				try {
					EntityEntity entity = em.find(EntityEntity.class, pk, ObjectPrivilegeMode.READ);
					entity.setTags(updatedTags.get(entity.getFacilityId()));
					updatedEntities.add(entity);
				} catch(Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(cloudScope.getCloudScopeId(), pk.getResourceId(), e);
				}
			}
			
			scope.complete();
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		return updatedEntities;
	}
	
	public static EntityUpdater updator() {
		return new EntityUpdater();
	}
	
	private CacheResourceManagement cacheRm;
	
	protected IResourceManagement getResourceManagement(LocationEntity location, CloudLoginUserEntity user) {
		if (cacheRm == null) {
			ICloudOption option = Session.current().get(ICloudOption.class);
			return option.getResourceManagement(location, user);
		} else {
			return cacheRm;
		}
	}

	public EntityUpdater setCacheResourceManagement(CacheResourceManagement rm) {
		this.cacheRm = rm;
		return this;
	}
}
