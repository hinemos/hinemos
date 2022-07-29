/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.HashMap;
import java.util.Map;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.factory.IResourceManagement.Entity;
import com.clustercontrol.xcloud.factory.IResourceManagement.Instance;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;

public class LocationRepositoryComparator {
	
	public interface DiffHandler {
		void foundMatchedFolder(IResourceManagement.Folder folder, FacilityTreeItem parentFacility, FacilityTreeItem folderFacility) throws CloudManagerException;
		void foundDiffFolder(IResourceManagement.Folder folder, FacilityTreeItem parentFacility) throws CloudManagerException;

		void foundDiffFacility(FacilityTreeItem parentFacility, FacilityTreeItem facility, IResourceManagement.Folder parentFolder) throws CloudManagerException;

		void foundMatchedInstance(IResourceManagement.ResourceHolder holder, IResourceManagement.Instance instance, FacilityTreeItem parentFacility, FacilityTreeItem instanceFacility) throws CloudManagerException;
		void foundDiffInstance(IResourceManagement.ResourceHolder holder, IResourceManagement.Instance instance, FacilityTreeItem parentFacility) throws CloudManagerException;

		void foundMatchedEntity(IResourceManagement.ResourceHolder holder, IResourceManagement.Entity entity, FacilityTreeItem parentFacility, FacilityTreeItem entityFacility) throws CloudManagerException;
		void foundDiffEntity(IResourceManagement.ResourceHolder holder, IResourceManagement.Entity entity, FacilityTreeItem parentFacility) throws CloudManagerException;
	}
	
	
	private Map<String, InstanceEntity> instanceMap = new HashMap<>();
	private Map<String, EntityEntity> entityMap = new HashMap<>();
	
	protected CloudScopeEntity cloudScope;
	protected CloudLoginUserEntity user;
	protected LocationEntity location;
	protected DiffHandler handler;
	
	public void compare(CloudLoginUserEntity user, LocationEntity location, IResourceManagement.Location platformLocation, FacilityTreeItem treeItem, DiffHandler handler) throws CloudManagerException {
		this.cloudScope = location.getCloudScope();
		this.user = user;
		this.location = location;
		this.handler = handler;
		
		recursiveCompareFolder(platformLocation, treeItem);
	}
	
	protected void recursiveCompareFolder(final IResourceManagement.Folder folder, final FacilityTreeItem treeItem) throws CloudManagerException {
		CollectionComparator.compare(folder.getElements(), treeItem.getChildren(), new CollectionComparator.Comparator<IResourceManagement.Element, FacilityTreeItem>() {
			@Override
			public boolean match(final IResourceManagement.Element o1, final FacilityTreeItem o2) throws CloudManagerException {
				return o1.transform(new IResourceManagement.Element.Transformer<Boolean>() {
					@Override
					public Boolean transform(IResourceManagement.Folder folder) throws CloudManagerException {
						return FacilityIdUtil.getFolderId(cloudScope.getId(), folder).equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE;
					}
					@Override
					public Boolean transform(IResourceManagement.ResourceHolder holder) throws CloudManagerException {
						return holder.getResource().transform(new IResourceManagement.Resource.Transformer<Boolean>() {
							@Override
							public Boolean transform(Instance instance) throws CloudManagerException {
								try {
									return getInstanceFacilityId(instance.getResourceId()).equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_NODE;
								} catch (CloudManagerException e) {
									return false;
								}
							}
							@Override
							public Boolean transform(Entity entity) throws CloudManagerException {
								try {
									return getEntityFacilityId(entity.getResourceId()).equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_NODE;
								} catch (CloudManagerException e) {
									return false;
								}
							}
						});
					}
				});
			}
			
			// リポジトリにリソース階層を構成する要素に該当するファシリティが存在する。
			@Override
			public void matched(final IResourceManagement.Element o1, final FacilityTreeItem o2) throws CloudManagerException {
				o1.visit(new IResourceManagement.Element.Visitor() {
					@Override
					public void visit(IResourceManagement.Folder folder) throws CloudManagerException {
						handler.foundMatchedFolder(folder, treeItem, o2);
						recursiveCompareFolder(folder, o2);
					}
					@Override
					public void visit(final IResourceManagement.ResourceHolder holder) throws CloudManagerException {
						holder.getResource().visit(new IResourceManagement.Resource.Visitor() {
							// リソースがインスタンスの場合
							@Override
							public void visit(Instance platformInstance) throws CloudManagerException {
								handler.foundMatchedInstance(holder, platformInstance, treeItem, o2);
							}
							// リソースがエンティティの場合
							@Override
							public void visit(Entity platformEntity) throws CloudManagerException {
								handler.foundMatchedEntity(holder, platformEntity, treeItem, o2);
							}
						});
					}
				});
			}
			// リポジトリに存在しないリソース階層を構成する要素が存在する。
			@Override
			public void afterO1(IResourceManagement.Element o1) throws CloudManagerException {
				// リポジトリ上にリソースに該当するノードの存在有無を考慮した更新を以下の関数はする。
				o1.visit(new IResourceManagement.Element.Visitor() {
					@Override
					public void visit(IResourceManagement.Folder folder) throws CloudManagerException {
						handler.foundDiffFolder(folder, treeItem);
					}
					@Override
					public void visit(final IResourceManagement.ResourceHolder holder) throws CloudManagerException {
						holder.getResource().visit(new IResourceManagement.Resource.Visitor() {
							// リソースがインスタンスの場合
							@Override
							public void visit(Instance platformInstance) throws CloudManagerException {
								handler.foundDiffInstance(holder, platformInstance, treeItem);
							}
							// リソースがエンティティの場合
							@Override
							public void visit(Entity platformEntity) throws CloudManagerException {
								handler.foundDiffEntity(holder, platformEntity, treeItem);
							}
						});
					}
				});
			}
			// リソース階層の構成要素に該当しないファシリティが存在する。
			@Override
			public void afterO2(FacilityTreeItem o2) throws CloudManagerException {
				handler.foundDiffFacility(treeItem, o2, folder);
			}
		});
	}
	
	protected InstanceEntity getInstanceEntity(String platformInstanceId) throws CloudManagerException {
		InstanceEntity instanceEntity = instanceMap.get(platformInstanceId);
		if (instanceEntity == null) {
			instanceEntity = CloudManager.singleton().getInstances(user, location).getInstance(platformInstanceId);
			instanceMap.put(platformInstanceId, instanceEntity);
		}
		return instanceEntity;
	}

	protected String getInstanceFacilityId(String platformInstanceId) throws CloudManagerException {
		InstanceEntity instanceEntity = getInstanceEntity(platformInstanceId);
		return FacilityIdUtil.getResourceId(cloudScope.getPlatformId(), cloudScope.getId(), instanceEntity.getResourceId());
	}
	
	protected EntityEntity getEntityEntity(String platformEntityId) throws CloudManagerException {
		EntityEntity entityEntity = entityMap.get(platformEntityId);
		if (entityEntity == null) {
			entityEntity = CloudManager.singleton().getInstances(user, location).getEntityByPlatformEntityId(platformEntityId);
			entityMap.put(platformEntityId, entityEntity);
		}
		return entityEntity;
	}

	protected String getEntityFacilityId(String platformEntityId) throws CloudManagerException {
		EntityEntity entityEntity = getEntityEntity(platformEntityId);
		return FacilityIdUtil.getEntityId(cloudScope.getId(), entityEntity.getEntityType(), entityEntity.getResourceId());
	}
	
	public static void compareLocationFolder(CloudLoginUserEntity user, LocationEntity location, IResourceManagement.Location platformLocation, FacilityTreeItem treeItem, DiffHandler handler) throws CloudManagerException {
		new LocationRepositoryComparator().compare(user, location, platformLocation, treeItem, handler);
	}
}
