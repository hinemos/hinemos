/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import static com.clustercontrol.xcloud.common.CloudConstants.Event_CloudScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.PostCommitAction;
import com.clustercontrol.xcloud.Threading;
import com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.AddCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AddPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AddPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntry;
import com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudScopeRequest;
import com.clustercontrol.xcloud.bean.ModifyPlatformServiceConditionRequest;
import com.clustercontrol.xcloud.bean.ModifyPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.ModifyPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.PrivateEndpoint;
import com.clustercontrol.xcloud.bean.PrivateLocation;
import com.clustercontrol.xcloud.bean.RoleRelation;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.ICloudOption.PlatformServiceCondition;
import com.clustercontrol.xcloud.factory.IPlatformServiceMonitor.ICloudScopeAreaMonitor;
import com.clustercontrol.xcloud.factory.IPlatformServiceMonitor.IPlatformAreaMonitor;
import com.clustercontrol.xcloud.factory.monitors.PlatformResourceMonitor;
import com.clustercontrol.xcloud.model.AutoAssignNodePatternEntryEntity;
import com.clustercontrol.xcloud.model.CloudScopeAreaServiceConditionEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionExecutor;
import com.clustercontrol.xcloud.model.CloudScopeEntity.Visitor;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.PlatformAreaServiceConditionEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.model.PrivateEndpointEntity;
import com.clustercontrol.xcloud.model.PrivateLocationEntity;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.TransactionException;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@Transactional
public class CloudScopes implements ICloudScopes {
	@Override
	public PublicCloudScopeEntity addPublicCloudScope(AddPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole {
		PublicCloudScopeEntity scope = new PublicCloudScopeEntity();
		addCloudScope(scope, request);
		return scope;
	}

	@Override
	public PrivateCloudScopeEntity addPrivateCloudScope(AddPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole {
		PrivateCloudScopeEntity scope = new PrivateCloudScopeEntity();

		for (PrivateLocation location: request.getPrivateLocations()) {
			PrivateLocationEntity locationEntity = new PrivateLocationEntity();
			locationEntity.setCloudScopeId(request.getCloudScopeId());
			locationEntity.setLocationId(location.getLocationId());
			locationEntity.setName(location.getName());
			
			for (PrivateEndpoint endpoint: location.getEndpoints()) {
				PrivateEndpointEntity endpointEntity = new PrivateEndpointEntity();
				endpointEntity.setCloudScopeId(request.getCloudScopeId());
				endpointEntity.setLocationId(location.getLocationId());
				endpointEntity.setEndpointId(endpoint.getEndpointId());
				endpointEntity.setUrl(endpoint.getUrl());
				
				locationEntity.getEndpoints().add(endpointEntity);
			}
			scope.getPrivateLocations().put(locationEntity.getLocationId(), locationEntity);
		}
		
		addCloudScope(scope, request);
		
		initialPlatformServiceConditions(scope);
		
		return scope;
	}

	private CloudScopeEntity addCloudScope(CloudScopeEntity scope, final AddCloudScopeRequest request) throws CloudManagerException, InvalidRole {
		scope.setPlatformId(request.getPlatformId());
		scope.setCloudScopeId(request.getCloudScopeId());
		scope.setName(request.getScopeName());
		scope.setOwnerRoleId(request.getOwnerRoleId());
		scope.setDescription(request.getDescription());
		
		// アカウントの作成を行う。
		AddCloudLoginUserRequest userRequest = new AddCloudLoginUserRequest();
		userRequest.setCloudScopeId(request.getCloudScopeId());
		userRequest.setLoginUserId(request.getAccount().getLoginUserId());
		userRequest.setUserName(request.getAccount().getUserName());
		userRequest.setDescription(request.getAccount().getDescription());
		userRequest.setCredential(request.getAccount().getCredential());
		userRequest.getRoleRelations().addAll(request.getAccount().getRoleRelations());
		
		boolean existed = false;
		for (RoleRelation rr: userRequest.getRoleRelations()) {
			if (rr.getRoleId().equals(request.getOwnerRoleId())) {
				existed = true;
				break;
			}
		}
		if (!existed) {
			RoleRelation rr = new RoleRelation();
			rr.setRoleId(request.getOwnerRoleId());
			userRequest.getRoleRelations().add(rr);
		}

		try (AddedEventNotifier<CloudScopeEntity> notifier = new AddedEventNotifier<>(CloudScopeEntity.class, Event_CloudScope, scope)) {
			HinemosEntityManager em = Session.current().getEntityManager();
			try {
				PersistenceUtil.persist(em, scope);
				em.flush();
			} catch (EntityExistsException e) {
				throw ErrorCode.CLOUDSCOPE_ALREADY_EXIST.cloudManagerFault(scope.getId());
			}
			notifier.setCompleted();
		}

		CloudManager.singleton().getLoginUsers().addAccount(userRequest);
		CloudManager.singleton().getRepository().createCloudScopeRepository(scope);
		
		scope.optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getCloudScopeListener().postAddCloudScope(scope, request);
			}
		});
		
		final String cloudScopeId = scope.getCloudScopeId();
		Session.current().addPostCommitAction(new PostCommitAction() {
			@Override
			public void postCommit() throws TransactionException {
				Threading.execute(new Runnable() {
					@Override
					public void run() {
						try {
							PlatformResourceMonitor.resourceUpdate(cloudScopeId);
						} catch (Exception e) {
							Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
						}
					}
				});
			}
		});
		return scope;
	}
	
	protected void initialPlatformServiceConditions(final CloudScopeEntity cloudScope) throws CloudManagerException {
		cloudScope.optionExecute(new CloudScopeEntity.OptionExecutor() {
			@Override
			public void execute(final CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getPlatformServiceMonitor().visit(new IPlatformServiceMonitor.Visitor() {
					@Override
					public void visit(IPlatformAreaMonitor monitor) throws CloudManagerException {
						HinemosEntityManager em = Session.current().getEntityManager();
						
						{
							List<PlatformServiceCondition> conditions = monitor.getPlatformServiceConditions();
							for (PlatformServiceCondition condition: conditions) {
								
								PlatformAreaServiceConditionEntity conditionEntity = new PlatformAreaServiceConditionEntity();
								conditionEntity.setPlatformId(monitor.getPlatformId());
								conditionEntity.setLocationId(cloudScope.getPlatformId());
								conditionEntity.setServiceId(condition.getServiceId());
								conditionEntity.setServiceName(condition.getServiceName());
								conditionEntity.setStatus(ICloudOption.PlatformServiceStatus.unknown);
								conditionEntity.setBeginDate(null);
								conditionEntity.setLastDate(null);
								
								PersistenceUtil.persist(em, conditionEntity);
							}
						}
						
						{
							for (LocationEntity location: cloudScope.getLocations()) {
								List<PlatformServiceCondition> conditions = monitor.getPlatformServiceConditions(location);
								for (PlatformServiceCondition condition: conditions) {
									
									PlatformAreaServiceConditionEntity conditionEntity = new PlatformAreaServiceConditionEntity();
									conditionEntity.setPlatformId(monitor.getPlatformId());
									conditionEntity.setLocationId(location.getLocationId());
									conditionEntity.setServiceId(condition.getServiceId());
									conditionEntity.setServiceName(condition.getServiceName());
									conditionEntity.setStatus(ICloudOption.PlatformServiceStatus.unknown);
									conditionEntity.setBeginDate(null);
									conditionEntity.setLastDate(null);
									
									PersistenceUtil.persist(em, conditionEntity);
								}
							}
						}
					}

					@Override
					public void visit(ICloudScopeAreaMonitor monitor) throws CloudManagerException {
						HinemosEntityManager em = Session.current().getEntityManager();
						{
							for (PlatformServiceCondition condition : monitor.getPlatformServiceConditions(cloudScope)) {
								CloudScopeAreaServiceConditionEntity conditionEntity = new CloudScopeAreaServiceConditionEntity();
								conditionEntity.setCloudScopeId(scope.getId());
								conditionEntity.setLocationId(cloudScope.getPlatformId());
								conditionEntity.setServiceId(condition.getServiceId());
								conditionEntity.setServiceName(condition.getServiceName());
								conditionEntity.setStatus(ICloudOption.PlatformServiceStatus.unknown);
								conditionEntity.setBeginDate(null);
								conditionEntity.setLastDate(null);
								
								PersistenceUtil.persist(em, conditionEntity);
							}
						}

						{
							for (LocationEntity location: cloudScope.getLocations()) {
								List<PlatformServiceCondition> conditions = monitor.getPlatformServiceConditions(cloudScope, location);
								for (PlatformServiceCondition condition: conditions) {
									
									PlatformAreaServiceConditionEntity conditionEntity = new PlatformAreaServiceConditionEntity();
									conditionEntity.setPlatformId(monitor.getPlatformId());
									conditionEntity.setLocationId(location.getLocationId());
									conditionEntity.setServiceId(condition.getServiceId());
									conditionEntity.setServiceName(condition.getServiceName());
									conditionEntity.setStatus(ICloudOption.PlatformServiceStatus.unknown);
									conditionEntity.setBeginDate(null);
									conditionEntity.setLastDate(null);
									
									PersistenceUtil.persist(em, conditionEntity);
								}
							}
						}
					}
				});
			}
		});
	}
	
	@Override
	public CloudScopeEntity removeCloudScope(String cloudScopeId) throws CloudManagerException, InvalidRole {
		HinemosEntityManager em = Session.current().getEntityManager();
		CloudScopeEntity scope = em.find(CloudScopeEntity.class, cloudScopeId, ObjectPrivilegeMode.READ);
		if (scope == null)
			throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(cloudScopeId);

		try (RemovedEventNotifier<CloudScopeEntity> notifier = new RemovedEventNotifier<>(CloudScopeEntity.class, Event_CloudScope, scope)) {
			String allNodeScopeId = FacilityIdUtil.getAllNodeScopeId(scope.getPlatformId(), scope.getId());
			
			TypedQuery<String> query = em.createNamedQuery("findChildFacilityIdList", String.class);
			query.setParameter("facilityId", allNodeScopeId);
			
			List<String> children = query.getResultList();
			RepositoryControllerBeanWrapper.bean().deleteNode(children.toArray(new String[children.size()]));
			
			String findChildScopeQuery = FacilityIdUtil.getCloudScopeScopeId(scope.getPlatformId(), scope.getCloudScopeId());
			query.setParameter("facilityId", findChildScopeQuery);
			List<String> childScopeList = query.getResultList();
			
			RepositoryControllerBean repositoryControllerBean = new RepositoryControllerBean();
			checkChildScope(repositoryControllerBean,query,childScopeList);
			
			CloudManager.singleton().getRepository().removeCloudScopeRepository(scope);
			em.remove(scope);
			notifier.completed();
		} catch (UsedFacility | HinemosUnknown | FacilityNotFound e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}
		
		scope.optionExecute(new OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getCloudScopeListener().postRemoveCloudScope(scope);
			}
		});
		return scope;
	}

	private void checkChildScope(RepositoryControllerBean repositoryControllerBean,TypedQuery<String> query,List<String> childScopeList) throws UsedFacility,HinemosUnknown,InvalidRole {
		for(String facilityId : childScopeList){
			repositoryControllerBean.checkIsUseFacilityWithChildren(facilityId);
			query.setParameter("facilityId", facilityId);
			List<String> getChildScopeResultList = query.getResultList();
			checkChildScope(repositoryControllerBean,query,getChildScopeResultList);
		}
	}

	@Override
	public CloudScopeEntity getCloudScope(String cloudScopeId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		CloudScopeEntity scope = em.find(CloudScopeEntity.class, cloudScopeId, ObjectPrivilegeMode.READ);
		if (scope == null)
			throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(cloudScopeId);
		return scope;
	}

	@Override
	public List<CloudScopeEntity> getAllCloudScopes() throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		return PersistenceUtil.findAll(em, CloudScopeEntity.class);
	}

	@Override
	public CloudScopeEntity getCloudScopeByHinemosUser(String cloudScopeId, String hinemosUserId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopeByHinemosUser", CloudScopeEntity.class);
		
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("userId", hinemosUserId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(cloudScopeId);
		}
	}
	

	@Override
	public CloudScopeEntity getCloudScopeByOwnerRole(String cloudScopeId, String ownerRoleId)
			throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopeByOwnerRole", CloudScopeEntity.class);
		
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("roleId", ownerRoleId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(cloudScopeId);
		}
	}
	
	@Override
	public List<CloudScopeEntity> getCloudScopesByHinemosUser(String hinemosUserId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopesByHinemosUser", CloudScopeEntity.class);
		query.setParameter("userId", hinemosUserId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
		return query.getResultList();
	}

	@Override
	public CloudScopeEntity getCloudScopeByCurrentHinemosUser(String cloudScopeId) throws CloudManagerException {
		return getCloudScopeByHinemosUser(cloudScopeId, Session.current().getHinemosCredential().getUserId());
	}

	@Override
	public List<CloudScopeEntity> getCloudScopesByCurrentHinemosUser() throws CloudManagerException {
		return getCloudScopesByHinemosUser(Session.current().getHinemosCredential().getUserId());
	}

	@Override
	public List<CloudScopeEntity> getCloudScopesByOwnerRole(String ownerRoleId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopesByOwnerRole", CloudScopeEntity.class);
		query.setParameter("roleId", ownerRoleId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
		return query.getResultList();
	}

	@Override
	public CloudScopeEntity modifyCloudScope(ModifyCloudScopeRequest request) throws CloudManagerException, InvalidRole {
		return request.transform(new ModifyCloudScopeRequest.ITransformer<CloudScopeEntity>() {
			@Override
			public CloudScopeEntity transform(final ModifyPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole {
				HinemosEntityManager em = Session.current().getEntityManager();
				CloudScopeEntity scope = em.find(CloudScopeEntity.class, request.getCloudScopeId(), ObjectPrivilegeMode.READ);
				if (scope == null)
					throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(request.getCloudScopeId());

				// FIXME 更新ユーザ、更新時刻を反映させるために時刻更新、次版ではログインユーザの更新の流れを見直すべき
				Long nowTime = HinemosTime.currentTimeMillis();
				scope.setUpdateDate(nowTime);
				
				scope.setName(request.getScopeName() != null ? request.getScopeName(): scope.getName());
				
				if (request.getDescription() != null)
					scope.setDescription(request.getDescription());
				
				scope.optionExecute(new OptionExecutor() {
					@Override
					public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
						option.getCloudScopeListener().postModifyCloudScope(scope, request);
					}
				});
				return scope;
			}

			@Override
			public CloudScopeEntity transform(final ModifyPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole {
				final HinemosEntityManager em = Session.current().getEntityManager();
				CloudScopeEntity scope = em.find(CloudScopeEntity.class, request.getCloudScopeId(), ObjectPrivilegeMode.READ);
				if (scope == null)
					throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(request.getCloudScopeId());

				// FIXME 更新ユーザ、更新時刻を反映させるために時刻更新、次版ではログインユーザの更新の流れを見直すべき
				Long nowTime = HinemosTime.currentTimeMillis();
				scope.setUpdateDate(nowTime);
				
				scope.setName(request.getScopeName() != null ? request.getScopeName(): scope.getName());
				
				if (request.getDescription() != null)
					scope.setDescription(request.getDescription());
				
				scope.visit(new Visitor() {
					@Override
					public void visit(final PrivateCloudScopeEntity scope) throws CloudManagerException {
						if (scope.getPrivateLocations() != null)
							CollectionComparator.compare(scope.getPrivateLocations().values(), request.getPrivateLocations(),
								new CollectionComparator.Comparator<PrivateLocationEntity, PrivateLocation>() {
									@Override
									public boolean match(PrivateLocationEntity l1, PrivateLocation l2) throws CloudManagerException {
										return l1.getLocationId().equals(l2.getLocationId());
									}
									@Override
									public void matched(final PrivateLocationEntity l1, final PrivateLocation l2) throws CloudManagerException {
										CollectionComparator.compare(l1.getEndpoints(), l2.getEndpoints(),
											new CollectionComparator.Comparator<PrivateEndpointEntity, PrivateEndpoint>() {
												@Override
												public boolean match(PrivateEndpointEntity e1, PrivateEndpoint e2) throws CloudManagerException {
													return e1.getEndpointId().equals(e2.getEndpointId());
												}
												@Override
												public void matched(PrivateEndpointEntity e1, PrivateEndpoint e2) throws CloudManagerException {
													e1.setUrl(e2.getUrl());
												}
												@Override
												public void afterO1(PrivateEndpointEntity e1) throws CloudManagerException {
													l1.getEndpoints().remove(e1);
													em.remove(e1);
												}
												@Override
												public void afterO2(PrivateEndpoint e2) throws CloudManagerException {
													PrivateEndpointEntity endpointEntity = new PrivateEndpointEntity();
													endpointEntity.setCloudScopeId(request.getCloudScopeId());
													endpointEntity.setLocationId(l1.getLocationId());
													endpointEntity.setEndpointId(e2.getEndpointId());
													endpointEntity.setUrl(e2.getUrl());
													l1.getEndpoints().add(endpointEntity);
												}
											}
										);
									}
									@Override
									public void afterO1(PrivateLocationEntity l1) throws CloudManagerException {
										scope.getPrivateLocations().remove(l1.getLocationId());
										em.remove(l1.getLocationId());
									}
									@Override
									public void afterO2(PrivateLocation l2) throws CloudManagerException {
										PrivateLocationEntity locationEntity = new PrivateLocationEntity();
										locationEntity.setCloudScopeId(request.getCloudScopeId());
										locationEntity.setLocationId(l2.getLocationId());
										locationEntity.setName(l2.getName());
										scope.getPrivateLocations().put(locationEntity.getLocationId(), locationEntity);
									}
								}
							);
					}
				});
				scope.optionExecute(new OptionExecutor() {
					@Override
					public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
						option.getCloudScopeListener().postModifyCloudScope(scope, request);
					}
				});
				return scope;
			}
		});
	}

	@Override
	public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> getPlatformServiceConditions(String cloudScopeId) throws CloudManagerException {
		return getPlatformServiceConditions(cloudScopeId, getCloudScope(cloudScopeId).getPlatformId());
	}

	@Override
	public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> getPlatformServiceConditions(String cloudScopeId, final String locationId) throws CloudManagerException {
		return getCloudScope(cloudScopeId).optionCall(new CloudScopeEntity.OptionCallable<List<com.clustercontrol.xcloud.bean.PlatformServiceCondition>>() {
			@Override
			public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> call(final CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				return option.getPlatformServiceMonitor().transform(new IPlatformServiceMonitor.ITransformer<List<com.clustercontrol.xcloud.bean.PlatformServiceCondition>>() {
					
					@Override
					public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> transform(ICloudScopeAreaMonitor monitor) throws CloudManagerException {
						List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> conditions = new ArrayList<>(); 
						for (PlatformServiceCondition conditionEntity: monitor.getPlatformServiceConditions(scope)) {
							com.clustercontrol.xcloud.bean.PlatformServiceCondition condition = new com.clustercontrol.xcloud.bean.PlatformServiceCondition();
							condition.setId(conditionEntity.getServiceId());
							condition.setServiceName(conditionEntity.getServiceName());
							condition.setStatus(conditionEntity.getStatus());
							condition.setMessage(conditionEntity.getMessage());
							condition.setDetail(conditionEntity.getDetail());
							condition.setBeginDate(conditionEntity.getMonitorDate().getTime());
							condition.setRecordDate(conditionEntity.getMonitorDate().getTime());
							condition.setLastDate(conditionEntity.getMonitorDate().getTime());
							conditions.add(condition);
						}
						return conditions;
					}
					
					@Override
					public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> transform(IPlatformAreaMonitor monitor) throws CloudManagerException {
						TypedQuery<PlatformAreaServiceConditionEntity> query = Session.current().getEntityManager().createNamedQuery("findPlatformAreaServiceConditionsByLocationId", PlatformAreaServiceConditionEntity.class);
						query.setParameter("platformId", monitor.getPlatformId());
						query.setParameter("locationId", locationId);
						List<PlatformAreaServiceConditionEntity> conditionEntities= query.getResultList();
						
						List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> conditions = new ArrayList<>(); 
						for (PlatformAreaServiceConditionEntity conditionEntity: conditionEntities) {
							com.clustercontrol.xcloud.bean.PlatformServiceCondition condition = new com.clustercontrol.xcloud.bean.PlatformServiceCondition();
							condition.setId(conditionEntity.getServiceId());
							condition.setServiceName(conditionEntity.getServiceName());
							condition.setStatus(conditionEntity.getStatus());
							condition.setMessage(conditionEntity.getMessage());
							condition.setDetail(conditionEntity.getDetail());
							condition.setBeginDate(conditionEntity.getBeginDate());
							condition.setLastDate(conditionEntity.getLastDate());
							condition.setRecordDate(conditionEntity.getRecordDate());
							conditions.add(condition);
						}
						return conditions;
					}
				});
			}
		});
	}
	
	@Override
	public List<AutoAssignNodePatternEntryEntity> registAutoAssigneNodePattern(String cloudScopeId, List<AutoAssignNodePatternEntry> patterns) {
		HinemosEntityManager em = Session.current().getEntityManager();
		
		TypedQuery<AutoAssignNodePatternEntryEntity> query = em.createNamedQuery("findAutoAssigneNodePatternsByCloudScopeId", AutoAssignNodePatternEntryEntity.class);
		query.setParameter("cloudScopeId", cloudScopeId);
		
		List<AutoAssignNodePatternEntryEntity> entities = query.getResultList();
		
		List<AutoAssignNodePatternEntryEntity> returns = new ArrayList<>();
		List<AutoAssignNodePatternEntryEntity> matchingEntries = new ArrayList<AutoAssignNodePatternEntryEntity>(entities);
		for (int i = 0; i < patterns.size(); ++i) {
			AutoAssignNodePatternEntry webEntry = patterns.get(i);
			
			Iterator<AutoAssignNodePatternEntryEntity> entryIter = matchingEntries.iterator();
			
			boolean found = false;
			while (entryIter.hasNext()) {
				AutoAssignNodePatternEntryEntity entry = entryIter.next();
				if (i == entry.getPriority()) {
					entry.setPatternType(webEntry.getPatternType());
					entry.setPattern(webEntry.getPattern());
					entry.setScopeId(webEntry.getScopeId());
					entryIter.remove();
					returns.add(entry);
					found = true;
					break;
				}
			}
			
			if (!found) {
				AutoAssignNodePatternEntryEntity entry = new AutoAssignNodePatternEntryEntity();
				entry.setCloudScopeId(cloudScopeId);
				entry.setScopeId(webEntry.getScopeId());
				entry.setPriority(i);
				entry.setPatternType(webEntry.getPatternType());
				entry.setPattern(webEntry.getPattern());
				PersistenceUtil.persist(em, entry);
				returns.add(entry);
			}
		}
		
		for (AutoAssignNodePatternEntryEntity entry: matchingEntries) {
			em.remove(entry);
		}
		return returns;
	}

	@Override
	public List<AutoAssignNodePatternEntryEntity> getAutoAssigneNodePatterns(String cloudScopeId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		
		TypedQuery<AutoAssignNodePatternEntryEntity> query = em.createNamedQuery("findAutoAssigneNodePatternsByCloudScopeId", AutoAssignNodePatternEntryEntity.class);
		query.setParameter("cloudScopeId", cloudScopeId);
		return query.getResultList();
	}

	@Override
	public List<AutoAssignNodePatternEntryEntity> clearAutoAssigneNodePattern(String cloudScopeId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		
		TypedQuery<AutoAssignNodePatternEntryEntity> query = em.createNamedQuery("findAutoAssigneNodePatternsByCloudScopeId", AutoAssignNodePatternEntryEntity.class);
		query.setParameter("cloudScopeId", cloudScopeId);
		
		List<AutoAssignNodePatternEntryEntity> entries = query.getResultList();
		for (AutoAssignNodePatternEntryEntity entry: entries) {
			em.remove(entry);
		}
		return entries;
	}

	@Override
	public CloudScopeEntity modifyBillingSetting(final ModifyBillingSettingRequest request) throws CloudManagerException, InvalidRole {
		HinemosEntityManager em = Session.current().getEntityManager();
		CloudScopeEntity entity = em.find(CloudScopeEntity.class, request.getCloudScopeId(), ObjectPrivilegeMode.READ);
		if (entity == null)
			throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(request.getCloudScopeId());

		entity.setBillingDetailCollectorFlg(request.isBillingDetailCollectorFlg());
		entity.setRetentionPeriod(request.getRetentionPeriod());
		entity.optionExecute(new CloudScopeEntity.OptionExecutor() {
			@Override
			public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				option.getBillingManagement(scope).updateBillingSetting(request);
			}
		});
		return entity;
	}

	@Override
	public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> modifyPlatformServiceCondition(
			ModifyPlatformServiceConditionRequest request) throws CloudManagerException {
		return getCloudScope(request.getCloudScopeId()).optionCall(new CloudScopeEntity.OptionCallable<List<com.clustercontrol.xcloud.bean.PlatformServiceCondition>>() {
		@Override
			public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> call(final CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				return option.getPlatformServiceMonitor().transform(new IPlatformServiceMonitor.ITransformer<List<com.clustercontrol.xcloud.bean.PlatformServiceCondition>>() {
					@Override
					public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> transform(ICloudScopeAreaMonitor monitor) throws CloudManagerException {
						return Collections.emptyList();
					}
					
					@Override
					public List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> transform(IPlatformAreaMonitor monitor) throws CloudManagerException {
						TypedQuery<PlatformAreaServiceConditionEntity> query = Session.current().getEntityManager().createNamedQuery("findPlatformAreaServiceConditionsByLocationId", PlatformAreaServiceConditionEntity.class);
						query.setParameter("platformId", monitor.getPlatformId());
						if (request.getLocationId() != null) {
							query.setParameter("locationId", request.getLocationId());
						} else {
							query.setParameter("locationId", monitor.getPlatformId());
						}
						List<PlatformAreaServiceConditionEntity> conditionEntities = query.getResultList();
						
						List<com.clustercontrol.xcloud.bean.PlatformServiceCondition> conditionList = new ArrayList<>(); 
						Long nowTime = HinemosTime.currentTimeMillis();
						for (PlatformAreaServiceConditionEntity conditionEntity : conditionEntities) {
							if (!request.getServiceIdList().contains(conditionEntity.getServiceId())) {
								continue;
							}

							conditionEntity.setStatus(request.getStatus());
							conditionEntity.setMessage(request.getMessage());
							conditionEntity.setDetail(request.getMessage());
							conditionEntity.setLastDate(nowTime);
							conditionEntity.setRecordDate(nowTime);

							com.clustercontrol.xcloud.bean.PlatformServiceCondition condition = new com.clustercontrol.xcloud.bean.PlatformServiceCondition();
							condition.setId(conditionEntity.getServiceId());
							condition.setServiceName(conditionEntity.getServiceName());
							condition.setStatus(conditionEntity.getStatus());
							condition.setMessage(conditionEntity.getMessage());
							condition.setDetail(conditionEntity.getDetail());
							condition.setBeginDate(conditionEntity.getBeginDate());
							condition.setLastDate(conditionEntity.getLastDate());
							condition.setRecordDate(conditionEntity.getRecordDate());
							conditionList.add(condition);

						}
						return conditionList;
					}
				});
			}
		});
	}
}
