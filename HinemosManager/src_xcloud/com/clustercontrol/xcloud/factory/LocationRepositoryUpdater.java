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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.IResourceManagement.Entity;
import com.clustercontrol.xcloud.factory.IResourceManagement.Folder;
import com.clustercontrol.xcloud.factory.IResourceManagement.Location;
import com.clustercontrol.xcloud.factory.Repository.RepositoryVisitor;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.ExtendedProperty;
import com.clustercontrol.xcloud.model.FacilityAdditionEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.clustercontrol.xcloud.util.RepositoryUtil;

/*
 * クラウドから取得したリソース情報を元に、リポジトリツリーを更新。
 */
public class LocationRepositoryUpdater {
	private Map<IResourceManagement.Instance, InstanceEntity> instanceMap = new HashMap<>();
	private Map<IResourceManagement.Entity, EntityEntity> entityMap = new HashMap<>();
	private Map<String, Folder> folders = new HashMap<>();
	
	
	// リソースアクセス用情報
	private CloudScopeEntity cloudScope;
	private LocationEntity location;
	private CloudLoginUserEntity user;
	
	private RepositoryVisitor visitor;

	public LocationRepositoryUpdater() {
	}
	
	protected InstanceEntity getInstanceEntity(final IResourceManagement.Instance platformInstance) throws CloudManagerException {
		InstanceEntity instanceEntity = instanceMap.get(platformInstance);
		if (instanceEntity == null) {
			instanceEntity = cloudScope.optionCall(new CloudScopeEntity.OptionCallable<InstanceEntity>() {
				public InstanceEntity call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					InstanceEntity instanceEntity = CloudManager.singleton().getInstances(user, location).getInstance(platformInstance.getResourceId());
					return instanceEntity;
				}
			});
			instanceMap.put(platformInstance, instanceEntity);
		}
		return instanceEntity;
	}
	
	protected EntityEntity getEntityEntity(IResourceManagement.Entity platformEntity) throws CloudManagerException {
		EntityEntity entityEntity = entityMap.get(platformEntity);
		if (entityEntity == null) {
			entityEntity = CloudManager.singleton().getInstances(user, location).getEntityByPlatformEntityId(platformEntity.getResourceId());
			entityMap.put(platformEntity, entityEntity);
		}
		return entityEntity;
	}
	
	private class UpdateBehavior {
		public void removeFacility(String parentFacilityId, FacilityTreeItem item) throws CloudManagerException {
			HinemosEntityManager em = Session.current().getEntityManager();
			FacilityAdditionEntity addition = em.find(FacilityAdditionEntity.class, item.getData().getFacilityId(), ObjectPrivilegeMode.READ);
			if (addition == null)
				return;
			
			if (!addition.getCloudScopeId().equals(cloudScope.getId()))
				return;
			
			try {
				RepositoryControllerBean bean = RepositoryControllerBeanWrapper.bean();
				switch (item.getData().getFacilityType()) {
				case FacilityConstant.TYPE_SCOPE:
					bean.deleteScope(new String[]{item.getData().getFacilityId()});
					break;
				case FacilityConstant.TYPE_NODE:
					bean.releaseNodeScope(parentFacilityId, new String[]{item.getData().getFacilityId()});
					break;
				default:
					break;
				}
			} catch (UsedFacility e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage());
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
		}
	}

	public void update(LocationEntity location, CloudLoginUserEntity user, IResourceManagement.Location platformLocation, RepositoryVisitor visitor) throws CloudManagerException {
		this.cloudScope = location.getCloudScope();
		this.location = location;
		this.user = user;
		this.visitor = visitor == null ? new RepositoryVisitor(): visitor;
		internalUpdate(platformLocation, new UpdateBehavior());
	}

	public void updateNotDeleting(LocationEntity location, CloudLoginUserEntity user, IResourceManagement.Location platformLocation, RepositoryVisitor visitor) throws CloudManagerException {
		this.cloudScope = location.getCloudScope();
		this.location = location;
		this.user = user;
		this.visitor = visitor == null ? new RepositoryVisitor(): visitor;
		internalUpdate(platformLocation, new UpdateBehavior() {
			@Override
			public void removeFacility(String parentFacilityId, FacilityTreeItem item) throws CloudManagerException {
				// 部分的な更新なので、削除せず、追加のみ。
			}
		});
	}
	
	protected void internalUpdate(IResourceManagement.Location platformLocation, final UpdateBehavior behavior) throws CloudManagerException {
		if (platformLocation == null)
			return;
		
		// 比較対象となるリポジトリの情報
		FacilityTreeItem treeItem;
		try {
			treeItem = RepositoryControllerBeanWrapper.bean().getFacilityTree(null, Locale.getDefault());
		} catch (HinemosUnknown e) {
			throw new InternalManagerError(e.getMessage(), e);
		}

		FacilityTreeItem locationItem = getLocationFacilityTreeItem(treeItem, behavior);

		// インスタンスおよびエンティティ情報の更新は、別トランザクションで行う。
		try (TransactionScope transaction = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
			try {
				RepositoryControllerBeanWrapper.bean().setAutoCommit(true);
				
				this.visitor.visitStart();
				
				LocationRepositoryComparator.compareLocationFolder(user, location, platformLocation, locationItem, new LocationRepositoryComparator.DiffHandler() {
					@Override
					public void foundMatchedFolder(IResourceManagement.Folder folder, FacilityTreeItem parentFacility, FacilityTreeItem folderFacility) throws CloudManagerException {
						try {
							folders.put(folderFacility.getData().getFacilityId(), folder);
							
							RepositoryControllerBeanWrapper bean = RepositoryControllerBeanWrapper.bean();
							ScopeInfo scopeInfo = bean.getScope(folderFacility.getData().getFacilityId());
							scopeInfo.setFacilityType(FacilityConstant.TYPE_SCOPE);
							ScopeInfo cloneScopeInfo = (ScopeInfo)scopeInfo.clone();
							
							cloneScopeInfo.setFacilityName(folder.getName());
	
							// クラウドオプション毎の Hinemos のスコープに対するカスタマイズ
							if (folder.canDecorate())
								folder.decorate(cloneScopeInfo);
							
							if (!scopeInfo.getFacilityName().equals(cloneScopeInfo.getFacilityName()) ||
								!scopeInfo.getDescription().equals(cloneScopeInfo.getDescription()) ||
								!scopeInfo.getIconImage().equals(cloneScopeInfo.getIconImage())
								) {
								bean.modifyScope(scopeInfo);
							}
							
							visitor.visitFolder(parentFacility.getData(), folderFacility.getData());
						} catch (FacilityNotFound | HinemosUnknown | InvalidRole | InvalidSetting e) {
							Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
						}
					}
					@Override
					public void foundMatchedInstance(final IResourceManagement.ResourceHolder holder, final IResourceManagement.Instance platformInstance, final FacilityTreeItem parentFacility, final FacilityTreeItem instanceFacility) throws CloudManagerException {
						cloudScope.optionExecute(new CloudScopeEntity.OptionExecutor() {
							@Override
							public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
								InstanceEntity instanceEntity = getInstanceEntity(platformInstance);
								visitor.visitInstance(parentFacility.getData(), instanceFacility.getData(), instanceEntity);
							}
						});
					}
					@Override
					public void foundMatchedEntity(final IResourceManagement.ResourceHolder holder, IResourceManagement.Entity entity, FacilityTreeItem parentFacility, FacilityTreeItem entityFacility) throws CloudManagerException {
						EntityEntity entityEntity = getEntityEntity(entity);
						visitor.visitEntity(parentFacility.getData(), entityFacility.getData(), entityEntity);
					}
					@Override
					public void foundDiffFolder(IResourceManagement.Folder folder, FacilityTreeItem parentFacility) throws CloudManagerException {
						recursiveBuildResourceRepository(folder, parentFacility.getData());
					}
					@Override
					public void foundDiffFacility(FacilityTreeItem parentFacility, FacilityTreeItem facility, Folder parentFolder) throws CloudManagerException {
						if (parentFolder instanceof Location && parentFolder.getLocation().getLocationEntity().getCloudScope() instanceof PrivateCloudScopeEntity) {
							CloudScopeEntity cloudScope = parentFolder.getLocation().getLocationEntity().getCloudScope();
							if (FacilityIdUtil.getCloudScopeNodeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId()).equals(facility.getData().getFacilityId()) ||
								FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId()).equals(facility.getData().getFacilityId())
								) {
								return;
							}
						}
						// OCI個別カスタマイズ
						// Entityを複数のロケーションスコープに割り当てているため
						// EPROP_SKIP_RELEASE_NODE_FACILITY_IDSに存在するファシリティIDは
						// スコープからリリースしない
						if (parentFolder.getLocation() != null) {
							for (com.clustercontrol.xcloud.factory.IResourceManagement.ExtendedProperty eprop : parentFolder
									.getLocation().getExtendedProperties()) {
								if (eprop.getName().equals(CloudConstants.EPROP_SKIP_RELEASE_NODE_FACILITY_IDS)
										&& eprop.getValue() != null) {
									List<String> facilityList = Arrays.asList(eprop.getValue().split(","));
									if (facilityList != null
											&& facilityList.contains(facility.getData().getFacilityId())) {
										return;
									} else {
										break;
									}
								}
							}
						}
						behavior.removeFacility(parentFacility.getData().getFacilityId(), facility);
					}
					@Override
					public void foundDiffInstance(final IResourceManagement.ResourceHolder holder, final IResourceManagement.Instance instance, final FacilityTreeItem parentFacility) throws CloudManagerException {
						newInstance(holder, instance, parentFacility.getData());
					}
					@Override
					public void foundDiffEntity(IResourceManagement.ResourceHolder holder, final IResourceManagement.Entity entity, FacilityTreeItem parentFacility) throws CloudManagerException {
						newEntity(holder, entity, parentFacility.getData());
					}
				});
				
				List<IResourceManagement.Resource> resources = new ArrayList<>();
				resources.addAll(platformLocation.getInstances());
				resources.addAll(platformLocation.getEntities());

				// エンティティーとインスタンスを "全ノード" スコープへ紐付け
				// "全ノード" スコープ取得
				List<FacilityTreeItem> treeItems = CloudUtil.collectScopes(treeItem, FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getId()));
				if (treeItems.isEmpty()) {
					RepositoryUtil.createAllNodeScope(cloudScope);
					
					// スコープへの自動アサイン
					assgineNodesToAllNodeScope(CloudUtil.emptyList(FacilityTreeItem.class), resources);
				} else {
					// スコープへの自動アサイン
					assgineNodesToAllNodeScope(treeItems.get(0).getChildren(), resources);
				}
				
				Session.current().getEntityManager().flush();
				
				// フォルダー情報更新
				List<String> facilityIds = new ArrayList<>(folders.keySet());
				
				if (facilityIds.size() != 0) {
					HinemosEntityManager em = Session.current().getEntityManager();
					
					TypedQuery<FacilityAdditionEntity> query = em.createNamedQuery(FacilityAdditionEntity.findFacilityAdditions, FacilityAdditionEntity.class);
					query.setParameter("facilityIds", facilityIds);
					List<FacilityAdditionEntity> result = query.getResultList();
					
					CollectionComparator.compare(result, folders.entrySet(), new CollectionComparator.Comparator<FacilityAdditionEntity, Map.Entry<String, IResourceManagement.Folder>>() {
						@Override
						public boolean match(FacilityAdditionEntity o1, Map.Entry<String, IResourceManagement.Folder> o2) throws CloudManagerException {
							return o1.getFacilityId().equals(o2.getKey());
						}
						@Override
						public void matched(final FacilityAdditionEntity entity, Map.Entry<String, IResourceManagement.Folder> o2) throws CloudManagerException {
							entity.setType(o2.getValue().getElementType());
							CollectionComparator.compare(entity.getExtendedProperties().values(), o2.getValue().getExtendedProperties(), new CollectionComparator.Comparator<ExtendedProperty, IResourceManagement.ExtendedProperty>() {
								@Override
								public boolean match(ExtendedProperty o1, IResourceManagement.ExtendedProperty o2) throws CloudManagerException {
									return o1.getName().equals(o2.getName());
								}
								@Override
								public void matched(ExtendedProperty o1, IResourceManagement.ExtendedProperty o2) throws CloudManagerException {
									o1.setDataType(o2.getDataType());
									o1.setValue(o2.getValue());
								}
								@Override
								public void afterO1(ExtendedProperty o1) throws CloudManagerException {
									entity.getExtendedProperties().remove(o1.getName());
								}
								@Override
								public void afterO2(IResourceManagement.ExtendedProperty o2) throws CloudManagerException {
									ExtendedProperty ep = new ExtendedProperty();
									ep.setName(o2.getName());
									ep.setDataType(o2.getDataType());
									ep.setValue(o2.getValue());
									entity.getExtendedProperties().put(o2.getName(), ep);
								}
							});
						}
						@Override
						public void afterO2(Map.Entry<String, IResourceManagement.Folder> o2) throws CloudManagerException {
							FacilityAdditionEntity fa = new FacilityAdditionEntity();
							fa.setFacilityId(o2.getKey());
							fa.setCloudScopeId(cloudScope.getCloudScopeId());
							fa.setType(o2.getValue().getElementType());
							for (IResourceManagement.ExtendedProperty rmep: o2.getValue().getExtendedProperties()) {
								ExtendedProperty ep = new ExtendedProperty();
								ep.setName(rmep.getName());
								ep.setDataType(rmep.getDataType());
								ep.setValue(rmep.getValue());
								fa.getExtendedProperties().put(rmep.getName(), ep);
							}
							Session.current().getEntityManager().flush();
							PersistenceUtil.persist(Session.current().getEntityManager(), fa);
						}
					});
				}
				
				visitor.visitEnd();
			} finally {
				RepositoryControllerBeanWrapper.bean().setAutoCommit(false);
			}
			transaction.complete();
		}
	}
	
	protected void assgineNodesToAllNodeScope(List<FacilityTreeItem> facilities, List<IResourceManagement.Resource> resources) throws CloudManagerException {
		CollectionComparator.compare(resources, facilities, new CollectionComparator.Comparator<IResourceManagement.Resource, FacilityTreeItem>() {
			// 既に "全ノードス" コープに追加されているか確認
			@Override
			public boolean match(IResourceManagement.Resource o1, final FacilityTreeItem o2) throws CloudManagerException {
				return o1.transform(new IResourceManagement.Resource.Transformer<Boolean>() {
					@Override
					public Boolean transform(IResourceManagement.Instance instance) throws CloudManagerException {
						try {
							InstanceEntity instanceEntity = getInstanceEntity(instance);
							return instanceEntity.getFacilityId() == null ? false: instanceEntity.getFacilityId().equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_NODE;
						} catch (CloudManagerException e) {
							return false;
						}
					}
					@Override
					public Boolean transform(Entity entity) throws CloudManagerException {
						try {
							EntityEntity entityEntity = getEntityEntity(entity);
							return entityEntity.getFacilityId() == null ? false: entityEntity.getFacilityId().equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_NODE;
						} catch (CloudManagerException e) {
							return false;
						}
					}
				});
			}

			// 新規に "全ノードス" コープへ追加
			@Override
			public void afterO1(IResourceManagement.Resource o1) throws CloudManagerException {
				try {
					o1.visit(new IResourceManagement.Resource.Visitor() {
						@Override
						public void visit(IResourceManagement.Instance instance) throws CloudManagerException {
							InstanceEntity instanceEntity = getInstanceEntity(instance);
							if (instanceEntity.getFacilityId() == null)
								return;
							final String nodeId = instanceEntity.getFacilityId();
							// 本セッションではAutoCommitがtrueのため、ノード登録と別トランザクションとなる
							try {
								RepositoryControllerBeanWrapper.bean().assignNodeScope(
										FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getId()),
										new String[]{nodeId});
							} catch (InvalidSetting | InvalidRole | HinemosUnknown e) {
								Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
							}
						}
						@Override
						public void visit(Entity entity) throws CloudManagerException {
							EntityEntity entityEntity = getEntityEntity(entity);
							if (entityEntity.getFacilityId() == null)
								return;
							final String nodeId = entityEntity.getFacilityId();
							// 本セッションではAutoCommitがtrueのため、ノード登録と別トランザクションとなる
							try {
								RepositoryControllerBeanWrapper.bean().assignNodeScope(
										FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getId()),
										new String[]{nodeId});
							} catch (InvalidSetting | InvalidRole | HinemosUnknown e) {
								Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
							}
						}
					});
				} catch (CloudManagerException e) {
					if (!ErrorCode.UNEXPECTED.match(e) && !ErrorCode.HINEMOS_MANAGER_ERROR.match(e) && e.getCause() == null) {
						if (Logger.getLogger(this.getClass()).isDebugEnabled()) {
							Logger.getLogger(this.getClass()).warn(HinemosMessage.replace(e.getMessage()), e);
						} else {
							Logger.getLogger(this.getClass()).warn(HinemosMessage.replace(e.getMessage()));
						}
					} else {
						Logger.getLogger(this.getClass()).warn(HinemosMessage.replace(e.getMessage()), e);
					}
				}
			}
		});
	}
	
	protected class Tracer extends RepositoryUtil.Tracer {
		public ScopeInfo root;
		public ScopeInfo cloudScopeScope;
		public ScopeInfo locationScope;
		
		@Override
		public void traceRoot(ScopeInfo scope) throws CloudManagerException {
			root = scope;
			visitor.visitCloudScopeRootScope(root);
		}
		@Override
		public void traceCloudScope(ScopeInfo scope, CloudScopeEntity cloudScope) throws CloudManagerException {
			cloudScopeScope = scope;
			visitor.visitCloudScopeScope(root, cloudScopeScope, cloudScope);
		}
		@Override
		public void traceLocation(ScopeInfo scope, LocationEntity location) throws CloudManagerException {
			locationScope = scope;
			visitor.visitLocationScope(cloudScopeScope, scope, location);
		}
	}
	
	protected FacilityTreeItem getLocationFacilityTreeItem(FacilityTreeItem top, final UpdateBehavior behavior) throws CloudManagerException {
		Tracer tracer = new Tracer();
		RepositoryUtil.createCloudScopeLocationScope(location, tracer);
		
		// 再帰的に更新処理を開始
		List<FacilityTreeItem> cloudScopeTreeItem = CloudUtil.collectScopes(top, tracer.locationScope != null ? tracer.locationScope.getFacilityId(): tracer.cloudScopeScope.getFacilityId());
		if (cloudScopeTreeItem.isEmpty())
			throw new InternalManagerError(String.format("Not found Scope of %s", tracer.locationScope != null ? tracer.locationScope.getFacilityId(): tracer.cloudScopeScope.getFacilityId()));
		
		return cloudScopeTreeItem.get(0);
	}
	
	protected void recursiveBuildResourceRepository(IResourceManagement.Element element, final FacilityInfo parent) throws CloudManagerException {
		element.visit(new IResourceManagement.Element.Visitor() {
			@Override
			public void visit(IResourceManagement.Folder folder) throws CloudManagerException {
				try {
					String facilityId = FacilityIdUtil.getResourceId(folder.getElementType(), cloudScope.getId(), folder.getId());
					folders.put(facilityId, folder);

					ScopeInfo scopeInfo = CloudUtil.createScope(
							facilityId,
							folder.getName(), cloudScope.getOwnerRoleId());
					
					// クラウドオプション毎の Hinemos のスコープに対するカスタマイズ
					if (folder.canDecorate())
						folder.decorate(scopeInfo);
					
					RepositoryControllerBeanWrapper.bean().addScope(parent.getFacilityId(), scopeInfo);
					
					visitor.visitFolder(parent, scopeInfo);

					// 配下の要素に対して処理をする
					for (IResourceManagement.Element element: folder.getElements()) {
						recursiveBuildResourceRepository(element, scopeInfo);
					}
				} catch (HinemosUnknown | InvalidRole | InvalidSetting | FacilityDuplicate e) {
					Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
				}
			}
			@Override
			public void visit(final IResourceManagement.ResourceHolder holder) throws CloudManagerException {
				holder.getResource().visit(new IResourceManagement.Resource.Visitor() {
					// リソースがインスタンスの場合
					@Override
					public void visit(final IResourceManagement.Instance platformInstance) throws CloudManagerException {
						newInstance(holder, platformInstance, parent);
					}
					// リソースがエンティティの場合
					@Override
					public void visit(final Entity platformEntity) throws CloudManagerException {
						newEntity(holder, platformEntity, parent);
					}
				});
			}
		});
	}
	
	public void newInstance(final IResourceManagement.ResourceHolder holder, final IResourceManagement.Instance platformInstance, final FacilityInfo parentFacility) throws CloudManagerException {
		try {
			final InstanceEntity instanceEntity = getInstanceEntity(platformInstance);
			if (instanceEntity.getFacilityId() == null)
				return;
			
			visitor.visitInstance(parentFacility, RepositoryControllerBeanWrapper.bean().getNode(instanceEntity.getFacilityId()), instanceEntity);
			
			// 本セッションではAutoCommitがtrueのため、ノード登録と別トランザクションとなる
			try {
				RepositoryControllerBeanWrapper.bean().assignNodeScope(parentFacility.getFacilityId(), new String[]{instanceEntity.getFacilityId()});
			} catch (InvalidSetting | InvalidRole | HinemosUnknown e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
		} catch (HinemosUnknown | FacilityNotFound e) {
			Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
		}
	}
	
	public void newEntity(final IResourceManagement.ResourceHolder holder, final IResourceManagement.Entity platformEntity, final FacilityInfo parentFacility) throws CloudManagerException {
		try {
			final EntityEntity entityEntity = getEntityEntity(platformEntity);
			if (entityEntity.getFacilityId() == null)
				return;
			
			visitor.visitEntity(parentFacility, RepositoryControllerBeanWrapper.bean().getNode(entityEntity.getFacilityId()), entityEntity);

			// 本セッションではAutoCommitがtrueのため、ノード登録と別トランザクションとなる
			try {
				RepositoryControllerBeanWrapper.bean().assignNodeScope(parentFacility.getFacilityId(), new String[]{entityEntity.getFacilityId()});
			} catch (InvalidSetting | InvalidRole | HinemosUnknown e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
		} catch (HinemosUnknown | FacilityNotFound e) {
			Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
		}
	}
	
	public static void updateRepository(LocationEntity location, CloudLoginUserEntity user, IResourceManagement.Location platformLocation, RepositoryVisitor visitor) throws CloudManagerException {
		new LocationRepositoryUpdater().update(location, user, platformLocation, visitor);
	}
	
	public static void updateRepositoryNotDeleting(LocationEntity location, CloudLoginUserEntity user, IResourceManagement.Location platformLocation, RepositoryVisitor visitor) throws CloudManagerException {
		new LocationRepositoryUpdater().updateNotDeleting(location, user, platformLocation, visitor);
	}
}
