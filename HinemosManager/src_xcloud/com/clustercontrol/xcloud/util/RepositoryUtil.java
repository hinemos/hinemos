/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import static com.clustercontrol.xcloud.common.CloudConstants.Node_CloudScope;
import static com.clustercontrol.xcloud.common.CloudConstants.Scope_All_Node;
import static com.clustercontrol.xcloud.common.CloudConstants.Scope_CloudScope;
import static com.clustercontrol.xcloud.common.CloudConstants.Scope_Location;
import static com.clustercontrol.xcloud.common.CloudConstants.Scope_Private_Root;
import static com.clustercontrol.xcloud.common.CloudConstants.Scope_Public_Root;
import static com.clustercontrol.xcloud.common.CloudConstants.privateRootId;
import static com.clustercontrol.xcloud.common.CloudConstants.publicRootId;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.FacilityRelationEntityPK;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.PostCommitAction;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.common.InternalIdCloud;
import com.clustercontrol.xcloud.factory.ActionMode;
import com.clustercontrol.xcloud.factory.AddedEventNotifier;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.CloudManager.OptionExecutor;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.model.AutoAssignNodePatternEntryEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.DataType;
import com.clustercontrol.xcloud.model.ExtendedProperty;
import com.clustercontrol.xcloud.model.FacilityAdditionEntity;
import com.clustercontrol.xcloud.model.IAssignableEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.ResourceTag;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.TransactionException;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.persistence.Transactional.TransactionOption;

import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;

public class RepositoryUtil {
	public static class Tracer {
		public void traceRoot(ScopeInfo rootScope) throws CloudManagerException {
		}
		public void traceCloudScope(ScopeInfo rootScope, CloudScopeEntity cloudScope) throws CloudManagerException {
		}
		public void traceCloudScopeNode(NodeInfo cloudScopeNode, CloudScopeEntity cloudScope) throws CloudManagerException {
		}
		public void traceLocation(ScopeInfo rootScope, LocationEntity location) throws CloudManagerException {
		}
	}
	
	public static void createLocationScopes(CloudScopeEntity cloudScope, RepositoryUtil.Tracer tracer) throws CloudManagerException {
		// 1 より多数のロケーションが存在する場合は、ロケーション用のスコープを作成。
		if (cloudScope.getLocations().size() > 1) {
			for (LocationEntity locationEntity: cloudScope.getLocations()) {
				createLocationScope(locationEntity, tracer);
			}
		}
	}

	public static void createLocationScope(LocationEntity locationEntity) throws CloudManagerException {
		createLocationScope(locationEntity, new Tracer());
	}
	public static void createLocationScope(final LocationEntity locationEntity, final RepositoryUtil.Tracer tracer) throws CloudManagerException {
		CloudManager.singleton().optionExecute(locationEntity.getCloudScope().getPlatformId(), new OptionExecutor() {
			@Override
			public void execute(ICloudOption option) throws CloudManagerException {
				try {
					ScopeCreationListener listener = new ScopeCreationListener() {
						@Override
						public void onCeated(ScopeInfo scopeInfo) {
							FacilityAdditionEntity fa = new FacilityAdditionEntity();
							fa.setFacilityId(scopeInfo.getFacilityId());
							fa.setCloudScopeId(locationEntity.getCloudScope().getCloudScopeId());
							fa.setType(CloudConstants.Scope_CloudScope);
							
							ExtendedProperty ep = new ExtendedProperty();
							ep.setName(CloudConstants.EPROP_CloudScope);
							ep.setDataType(DataType.string);
							ep.setValue(locationEntity.getCloudScope().getId());
							fa.getExtendedProperties().put(ep.getName(), ep);
							
							ExtendedProperty ep2 = new ExtendedProperty();
							ep2.setName(CloudConstants.EPROP_Platform);
							ep2.setDataType(DataType.string);
							ep2.setValue(locationEntity.getCloudScope().getPlatformId());
							fa.getExtendedProperties().put(ep2.getName(), ep2);
							
							ExtendedProperty ep3 = new ExtendedProperty();
							ep3.setName(CloudConstants.EPROP_Location);
							ep3.setDataType(DataType.string);
							ep3.setValue(locationEntity.getLocationId());
							fa.getExtendedProperties().put(ep3.getName(), ep3);
							
							Session.current().getEntityManager().flush();
							PersistenceUtil.persist(Session.current().getEntityManager(), fa);
						}
					};
					
					String cloudScopeScopeId = FacilityIdUtil.getCloudScopeScopeId(locationEntity.getCloudScope());
					String locationScopeId = FacilityIdUtil.getLocationScopeId(locationEntity.getCloudScope().getId(), locationEntity);
					ScopeInfo scopeInfo = createScopeIfNotExist(Scope_Location, cloudScopeScopeId, locationScopeId, locationEntity.getName(), locationEntity.getCloudScope().getOwnerRoleId(), false, listener);
					tracer.traceLocation(scopeInfo, locationEntity);
				} catch (CloudManagerException e) {
					throw e;
				} catch (Exception e) {
					throw new CloudManagerException(e);
				}
			}
		});
	}
	
	interface ScopeCreationListener {
		void onCeated(ScopeInfo scopeInfo) throws CloudManagerException;
	}
	
	public static ScopeInfo createScopeIfNotExist(String eventType, String parentId, String cloudScopeId, String scopeName, String ownerRoleId, boolean builtin, ScopeCreationListener listener) throws CloudManagerException {
		ScopeInfo scopeInfo;
		try {
			scopeInfo = RepositoryControllerBeanWrapper.bean().getScope(cloudScopeId);
			
			if (parentId != null) {
				TypedQuery<String> query = Session.current().getEntityManager().createNamedQuery(InstanceEntity.findParentsOfFacility, String.class);
				query.setParameter("facilityId", cloudScopeId);
				
				String result;
				try {
					result = query.getSingleResult();
				} catch (NoResultException e) {
					throw ErrorCode.HINEMOS_SCOPE_DUPLICATED.cloudManagerFault(cloudScopeId);
				} catch (NonUniqueResultException  e) {
					// クラウドスコープ配下のスコープが複数の親を持つことはない
					// そのため、ここにたどり着く場合、クラウドスコープ（もしくは配下のスコープ）と
					// 重複するファシリティIDを持つファシリティが既に存在することを示す。
					throw ErrorCode.SCOPE_FACILITY_DUPLICATE.cloudManagerFault(cloudScopeId);
				}

				if (!parentId.equals(result)) {
					throw ErrorCode.HINEMOS_SCOPE_DUPLICATED.cloudManagerFault(cloudScopeId);
				}
			}
		} catch (FacilityNotFound e1) {
			scopeInfo = CloudUtil.createScope(cloudScopeId, scopeName, ownerRoleId);
			try (AddedEventNotifier<ScopeInfo> notifier = new AddedEventNotifier<>(ScopeInfo.class, eventType, scopeInfo)) {
				RepositoryControllerBeanWrapper.bean().addScope(parentId, scopeInfo);
				
				if (listener != null)
					listener.onCeated(scopeInfo);
				notifier.setCompleted();
			} catch (Exception e) {
				throw new CloudManagerException(e);
			}
		} catch (CloudManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new CloudManagerException(e);
		}
		return scopeInfo;
	}
	
	public static ScopeInfo createPublicRootScope() throws CloudManagerException {
		return createPublicRootScope(new Tracer());
	}
	public static ScopeInfo createPublicRootScope(RepositoryUtil.Tracer tracer) throws CloudManagerException {
		String facilityName = CloudMessageConstant.PUBLIC_CLOUDROOT_SCOPE_NAME.getMessage();
		// マネージャ起動時のCloudManagerServiceの有効化処理で生成するため基本的に既に存在するはず
		ScopeInfo scope = createScopeIfNotExist(Scope_Public_Root, null, publicRootId, facilityName, RoleIdConstant.ADMINISTRATORS, true, 
			s->{
				AccessControllerBean access = new AccessControllerBean();
				
				ObjectPrivilegeInfo priv1 = new ObjectPrivilegeInfo();
				priv1.setRoleId("ALL_USERS");
				priv1.setObjectType("PLT_REP");
				priv1.setObjectId(publicRootId);
				priv1.setObjectPrivilege("READ");
				try {
					access.replaceObjectPrivilegeInfo("PLT_REP", publicRootId, Arrays.asList(priv1));
				} catch (Exception e) {
					throw new CloudManagerException(e);
				}
			});
		tracer.traceRoot(scope);
		return scope;
	}
	
	public static ScopeInfo createPrivateRootScope() throws CloudManagerException {
		return createPrivateRootScope(new Tracer());
	}
	public static ScopeInfo createPrivateRootScope(RepositoryUtil.Tracer tracer) throws CloudManagerException {
		String facilityName = CloudMessageConstant.PRIVATE_CLOUDROOT_SCOPE_NAME.getMessage();
		// マネージャ起動時のCloudManagerServiceの有効化処理で生成するため基本的に既に存在するはず
		ScopeInfo scope = createScopeIfNotExist(Scope_Private_Root, null, privateRootId, facilityName, RoleIdConstant.ADMINISTRATORS, true,
			s->{
				AccessControllerBean access = new AccessControllerBean();
				
				ObjectPrivilegeInfo priv1 = new ObjectPrivilegeInfo();
				priv1.setRoleId("ALL_USERS");
				priv1.setObjectType("PLT_REP");
				priv1.setObjectId(privateRootId);
				priv1.setObjectPrivilege("READ");
				try {
					access.replaceObjectPrivilegeInfo("PLT_REP", privateRootId, Arrays.asList(priv1));
				} catch (Exception e) {
					throw new CloudManagerException(e);
				}
			});
		tracer.traceRoot(scope);
		return scope;
	}

	public static void createCloudScopeScope(final CloudScopeEntity cloudScope, final RepositoryUtil.Tracer tracer) throws CloudManagerException {
		CloudManager.singleton().optionExecute(cloudScope.getPlatformId(), new OptionExecutor() {
			@Override
			public void execute(ICloudOption option) throws CloudManagerException {
				try {
					ScopeCreationListener listener = new ScopeCreationListener() {
						@Override
						public void onCeated(ScopeInfo scopeInfo) {
							FacilityAdditionEntity fa = new FacilityAdditionEntity();
							fa.setFacilityId(scopeInfo.getFacilityId());
							fa.setCloudScopeId(cloudScope.getCloudScopeId());
							fa.setType(CloudConstants.Scope_CloudScope);
							
							ExtendedProperty ep = new ExtendedProperty();
							ep.setName(CloudConstants.EPROP_CloudScope);
							ep.setDataType(DataType.string);
							ep.setValue(cloudScope.getId());
							fa.getExtendedProperties().put(ep.getName(), ep);
							
							ExtendedProperty ep2 = new ExtendedProperty();
							ep2.setName(CloudConstants.EPROP_Platform);
							ep2.setDataType(DataType.string);
							ep2.setValue(cloudScope.getPlatformId());
							fa.getExtendedProperties().put(ep2.getName(), ep2);

							Session.current().getEntityManager().flush();
							PersistenceUtil.persist(Session.current().getEntityManager(), fa);
						}
					};
					
					if (option.getCloudSpec().isPublicCloud()) {
						String cloudScopeScopeId = FacilityIdUtil.getCloudScopeScopeId(cloudScope);
						ScopeInfo scope = createScopeIfNotExist(Scope_CloudScope, publicRootId, cloudScopeScopeId, cloudScope.getName(), cloudScope.getOwnerRoleId(), false, listener);
						tracer.traceCloudScope(scope, cloudScope);
					} else {
						String cloudScopeScopeId = FacilityIdUtil.getCloudScopeScopeId(cloudScope);
						ScopeInfo scope = createScopeIfNotExist(Scope_CloudScope, privateRootId, cloudScopeScopeId, cloudScope.getName(), cloudScope.getOwnerRoleId(), false, listener);
						tracer.traceCloudScope(scope, cloudScope);
					}
				} catch (CloudManagerException e) {
					throw e;
				} catch (Exception e) {
					throw new CloudManagerException(e);
				}
			}
		});
	}
	
	public static void createCloudScopeLocationScope(LocationEntity location) throws CloudManagerException {
		createCloudScopeLocationScope(location, new Tracer());
	}

	public static void createCloudScopeLocationScope(final LocationEntity location, final RepositoryUtil.Tracer tracer) throws CloudManagerException {
		CloudManager.singleton().optionExecute(location.getCloudScope().getPlatformId(), new OptionExecutor() {
			@Override
			public void execute(ICloudOption option) throws CloudManagerException {
				if (option.getCloudSpec().isPublicCloud()) {
					createPublicRootScope(tracer);
				} else {
					createPrivateRootScope(tracer);
				}
				
				// クラウドスコープに対応したスコープ作成。
				createCloudScopeScope(location.getCloudScope(), tracer);

				if (location.getCloudScope().getLocations().size() > 1) {
					// ロケーション用のスコープ作成。
					createLocationScope(location, tracer);
				}
				// 全ノード用のスコープ作成。
				createAllNodeScope(location.getCloudScope());
				
				createCloudScopeNodeIfExist(location.getCloudScope(), tracer);
			}
		});
	}
	
	public static void createAllNodeScope(CloudScopeEntity cloudScope) throws CloudManagerException {
		String allNodeName = CloudMessageConstant.ALL_NODE_SCOPE_NAME.getMessage();
		String cloudScopeScopeId = FacilityIdUtil.getCloudScopeScopeId(cloudScope);
		createScopeIfNotExist(Scope_All_Node, cloudScopeScopeId, FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getId()), allNodeName, cloudScope.getOwnerRoleId(), false, null);
	}
	
	public static void createCloudScopeRepository(CloudScopeEntity cloudScope) throws CloudManagerException {
		createCloudScopeRepository(cloudScope, new Tracer());
	}
	
	public static void createCloudScopeNodeIfExist(CloudScopeEntity cloudScope) throws CloudManagerException {
		createCloudScopeNodeIfExist(cloudScope, new Tracer());
	}

	public static void createCloudScopeNodeIfExist(CloudScopeEntity cloudScope, final RepositoryUtil.Tracer tracer) throws CloudManagerException {
		final String nodeId = FacilityIdUtil.getCloudScopeNodeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());

		HinemosEntityManager em = Session.current().getEntityManager();
		FacilityAdditionEntity fa = em.find(FacilityAdditionEntity.class, nodeId, ObjectPrivilegeMode.READ);
		if (fa == null) {
			NodeInfo nodeInfo = null;
			try {
				nodeInfo  = RepositoryControllerBeanWrapper.bean().getNodeFull(nodeId);
				String scopeId = FacilityIdUtil.getCloudScopeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
				associateIfNot(scopeId, nodeId);
				String allNodescopeId = FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
				associateIfNot(allNodescopeId, nodeId);
			} catch (FacilityNotFound e1) {
				nodeInfo = CloudUtil.createNodeInfo(
						nodeId,
						cloudScope.getName() == null || cloudScope.getName().isEmpty() ? cloudScope.getCloudScopeId(): cloudScope.getName(),
						cloudScope.getPlatformId(),
						cloudScope.getPlatformId(),
						cloudScope.getName() == null || cloudScope.getName().isEmpty() ? cloudScope.getCloudScopeId(): cloudScope.getName(),
						"Hinemos Auto Registered",
						cloudScope.getPlatformId(),
						cloudScope.getId(),
						cloudScope.getName() == null || cloudScope.getName().isEmpty() ? cloudScope.getCloudScopeId(): cloudScope.getName(),
						"CloudScope", // Hard-code
						cloudScope.getCloudScopeId(),
						"",
						cloudScope.getOwnerRoleId());
				// Avoid Scope node device search
				nodeInfo.setAutoDeviceSearch(Boolean.FALSE);
				try {
					RepositoryControllerBeanWrapper.bean().addNode(nodeInfo);
					
					// 新規作成のノードを同一トランザクションでスコープに紐付けられないのでコミット後に紐付け
					final String scopeId = FacilityIdUtil.getCloudScopeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
					final String allNodescopeId = FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
					Session.current().addPostCommitAction(new PostCommitAction() {
						@Override
						public void postCommit() throws TransactionException {
							try {
								RepositoryControllerBeanWrapper.bean().assignNodeScope(scopeId, new String[]{nodeId});
								RepositoryControllerBeanWrapper.bean().assignNodeScope(allNodescopeId, new String[]{nodeId});
							} catch (InvalidSetting | InvalidRole | HinemosUnknown e) {
								Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
							}
						}
					});
				} catch (FacilityDuplicate e2) {
					try {
						nodeInfo  = RepositoryControllerBeanWrapper.bean().getNode(nodeId);
						String scopeId = FacilityIdUtil.getCloudScopeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
						associateIfNot(scopeId, nodeId);
						String allNodescopeId = FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
						associateIfNot(allNodescopeId, nodeId);
					} catch (FacilityNotFound | HinemosUnknown e3) {
						throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e3);
					}
				} catch (InvalidSetting | HinemosUnknown e2) {
					throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e2);
				}
			} catch (HinemosUnknown e1) {
				throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e1);
			}
			
			try (AddedEventNotifier<NodeInfo> notifier = new AddedEventNotifier<>(NodeInfo.class, Node_CloudScope, nodeInfo)) {
				fa = new FacilityAdditionEntity();
				fa.setFacilityId(nodeInfo.getFacilityId());
				fa.setCloudScopeId(cloudScope.getCloudScopeId());
				fa.setType(CloudConstants.Node_CloudScope);
				
				ExtendedProperty ep = new ExtendedProperty();
				ep.setName(CloudConstants.EPROP_CloudScope);
				ep.setDataType(DataType.string);
				ep.setValue(cloudScope.getId());
				fa.getExtendedProperties().put(ep.getName(), ep);
				
				ExtendedProperty ep2 = new ExtendedProperty();
				ep2.setName(CloudConstants.EPROP_Platform);
				ep2.setDataType(DataType.string);
				ep2.setValue(cloudScope.getPlatformId());
				fa.getExtendedProperties().put(ep2.getName(), ep2);
				
				Session.current().getEntityManager().flush();
				PersistenceUtil.persist(Session.current().getEntityManager(), fa);
				
				tracer.traceCloudScopeNode(nodeInfo, cloudScope);

				notifier.setCompleted();
			}
		} else {
			String scopeId = FacilityIdUtil.getCloudScopeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
			associateIfNot(scopeId, nodeId);
			String allNodescopeId = FacilityIdUtil.getAllNodeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
			associateIfNot(allNodescopeId, nodeId);
			
			try {
				NodeInfo nodeInfo = RepositoryControllerBeanWrapper.bean().getNode(nodeId);
				tracer.traceCloudScopeNode(nodeInfo, cloudScope);
			} catch (FacilityNotFound | HinemosUnknown e) {
				throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
			}
		}
	}
	
	protected static void associateIfNot(String scopeId, String nodeId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		FacilityRelationEntity fr = em.find(FacilityRelationEntity.class, new FacilityRelationEntityPK(scopeId, nodeId), ObjectPrivilegeMode.READ);
		if (fr == null) {
			try {
				RepositoryControllerBeanWrapper.bean().assignNodeScope(scopeId, new String[] {nodeId});
			} catch (InvalidSetting | InvalidRole | HinemosUnknown e) {
				throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
			}
		}
	}
	
	public static void createCloudScopeRepository(final CloudScopeEntity cloudScope, final RepositoryUtil.Tracer tracer) throws CloudManagerException {
		CloudManager.singleton().optionExecute(cloudScope.getPlatformId(), new OptionExecutor() {
			@Override
			public void execute(ICloudOption option) throws CloudManagerException {
				if (option.getCloudSpec().isPublicCloud()) {
					createPublicRootScope(tracer);
				} else {
					createPrivateRootScope(tracer);
				}
				// クラウドスコープに対応したスコープ作成。
				createCloudScopeScope(cloudScope, tracer);

				createLocationScopes(cloudScope, tracer);

				// 全ノード格納用スコープ作成。
				createAllNodeScope(cloudScope);
				
				createCloudScopeNodeIfExist(cloudScope, tracer);
			}
		});
	}
	
	public static void removeCloudScopeRepository(CloudScopeEntity cloudScope) throws CloudManagerException {
		try {
			RepositoryControllerBeanWrapper.bean().deleteScope(new String[]{FacilityIdUtil.getCloudScopeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId())});
		} catch(Exception e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}
	}
	
	public static void autoAssgineNodesToScope(CloudScopeEntity cloudScope, List<IAssignableEntity> entities) throws CloudManagerException {
		try (TransactionScope scope = new TransactionScope(TransactionOption.RequiredNew)) {
			Map<String, List<String>> matchedNodeMap = new LinkedHashMap<>();
			
			List<AutoAssignNodePatternEntryEntity> entries = CloudManager.singleton().getCloudScopes().getAutoAssigneNodePatterns(cloudScope.getId());
			
			List<IAssignableEntity> checkingEntities = new ArrayList<>(entities);
			Boolean multiFlg = HinemosPropertyCommon.xcloud_assign_to_multiple_scope.getBooleanValue();
			
			for (AutoAssignNodePatternEntryEntity entry: entries) {
				switch (entry.getPatternType()) {
				case instanceName:
					{
						Pattern p = Pattern.compile(entry.getPattern());
						
						Iterator<IAssignableEntity> entityIter = checkingEntities.iterator();
						while (entityIter.hasNext()) {
							IAssignableEntity entity = entityIter.next();
							if (!entity.isPatternEnabled() || entity.getFacilityId() == null)
								continue;
							
							if (p.matcher(entity.getName()).matches()) {
								List<String> matchedNodes = matchedNodeMap.get(entry.getScopeId());
								if (matchedNodes == null) {
									matchedNodes = new ArrayList<>();
									matchedNodeMap.put(entry.getScopeId(), matchedNodes);
								}
								matchedNodes.add(entity.getFacilityId());
								if(!multiFlg)
									entityIter.remove();
							}
						}
					}
					break;
				case cidr:
					{
						try {
							Cidr c = new Cidr(entry.getPattern());
							Iterator<IAssignableEntity> entityIter = checkingEntities.iterator();
							while (entityIter.hasNext()) {
								IAssignableEntity entity = entityIter.next();
								if (!entity.isPatternEnabled() || entity.getFacilityId() == null)
									continue;
								
								for (String ip: entity.getIpAddresses()) {
									if (c.matches(ip)) {
										List<String> matchedNodes = matchedNodeMap.get(entry.getScopeId());
										if (matchedNodes == null) {
											matchedNodes = new ArrayList<>();
											matchedNodeMap.put(entry.getScopeId(), matchedNodes);
										}
										matchedNodes.add(entity.getFacilityId());
										if(!multiFlg)
											entityIter.remove();
									}
								}
							}
						} catch (UnknownHostException e) {
							Logger.getLogger(RepositoryUtil.class).warn(e.getMessage(), e);
						}
					}
					break;
				}
				
				if (checkingEntities.isEmpty())
					break;
			}
			
			for (IAssignableEntity entity: entities) {
				if (!entity.isTagEnabled() || entity.getFacilityId() == null)
					continue;

				String scopesString = null;
				for (ResourceTag t : entity.getTags().values()) {
					if (t.getKey().equals("hinemosAssignScopeId")) {
						scopesString = t.getValue();
						break;
					}
				}

				if (scopesString != null) {
					String[] scopeIds = scopesString.split(",");
					for (String scopeId : scopeIds) {
						List<String> matchedNodes = matchedNodeMap.get(scopeId);
						if (matchedNodes == null) {
							matchedNodes = new ArrayList<>();
							matchedNodeMap.put(scopeId, matchedNodes);
						}
						matchedNodes.add(entity.getFacilityId());
					}
				}
			}
			
			for (final Map.Entry<String, List<String>> entry: matchedNodeMap.entrySet()) {
				List<FacilityInfo> children;
				try {
					try {
						// スコープの存在チェック
						RepositoryControllerBeanWrapper.bean().getScope(entry.getKey());
					} catch (FacilityNotFound e) {
						Logger logger = Logger.getLogger(RepositoryUtil.class);
						logger.warn(String.format("Not found scope to relate to nodes. scopeId=%s,nodeIds=%s", entry.getKey(), entry.getValue()));
						String[] args = {entry.getValue().toString(),entry.getKey()};
						//internal event notify
						CloudUtil.notifyInternalMessage(
								InternalIdCloud.CLOUD_SYS_002,
								args,
								""
								);
						continue;
					}
					
					children = RepositoryControllerBeanWrapper.bean().getFacilityList(entry.getKey());
					CollectionComparator.compare(entry.getValue(), children, new CollectionComparator.Comparator<String, FacilityInfo>() {
						@Override
						public boolean match(String o1, FacilityInfo o2) throws CloudManagerException {
							return o1.equals(o2.getFacilityId());
						}
						@Override
						public void afterO1(String o1) throws CloudManagerException {
							try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
								if (ActionMode.isAutoDetection()) {
									Logger.getLogger(this.getClass()).info(
										String.format("Assign Node, Method=autoRegist, HinemosUser=%s, ParentFacilityID=%s, AssginFacilityID=%s",
												Session.current().getHinemosCredential().getUserId(), entry.getKey(), o1));
								}
								
								RepositoryControllerBeanWrapper.bean().assignNodeScope(entry.getKey(), new String[] { o1 });
								
								scope.complete();
							} catch (Exception e) {
								Logger logger = Logger.getLogger(this.getClass());
								logger.error(e.getMessage(), e);
								CloudUtil.notifyInternalMessage(
										InternalIdCloud.CLOUD_SYS_001,
										CloudMessageUtil.getExceptionStackTrace(e)
								);
							}
						}
					});
				} catch (HinemosUnknown | InvalidRole e) {
					Logger.getLogger(RepositoryUtil.class).warn(e.getMessage(), e);
					CloudUtil.notifyInternalMessage(
							InternalIdCloud.CLOUD_SYS_001,
							CloudMessageUtil.getExceptionStackTrace(e)
					);
				}
			}
		}
	}
}
