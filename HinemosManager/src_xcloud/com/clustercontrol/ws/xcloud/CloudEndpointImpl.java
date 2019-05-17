/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ws.xcloud;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.TypedQuery;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.util.VersionUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.ws.xcloud.security.HinemosAccessRight;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.AddCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AddCloudScopeRequest.ITransformer;
import com.clustercontrol.xcloud.bean.AddPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AddPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntry;
import com.clustercontrol.xcloud.bean.AvailableRole;
import com.clustercontrol.xcloud.bean.BillingResult;
import com.clustercontrol.xcloud.bean.CloneBackupedInstanceRequest;
import com.clustercontrol.xcloud.bean.CloneBackupedStorageRequest;
import com.clustercontrol.xcloud.bean.CloudLoginUser;
import com.clustercontrol.xcloud.bean.CloudPlatform;
import com.clustercontrol.xcloud.bean.CloudScope;
import com.clustercontrol.xcloud.bean.CreateInstanceSnapshotRequest;
import com.clustercontrol.xcloud.bean.CreateStorageSnapshotRequest;
import com.clustercontrol.xcloud.bean.HRepository;
import com.clustercontrol.xcloud.bean.Instance;
import com.clustercontrol.xcloud.bean.InstanceBackup;
import com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudScopeRequest;
import com.clustercontrol.xcloud.bean.ModifyInstanceRequest;
import com.clustercontrol.xcloud.bean.Network;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.bean.PlatformServiceCondition;
import com.clustercontrol.xcloud.bean.PlatformUser;
import com.clustercontrol.xcloud.bean.PrivateCloudScope;
import com.clustercontrol.xcloud.bean.PublicCloudScope;
import com.clustercontrol.xcloud.bean.RoleRelation;
import com.clustercontrol.xcloud.bean.Storage;
import com.clustercontrol.xcloud.bean.StorageBackup;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudRoleConstants;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.ActionMode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.ICloudScopes;
import com.clustercontrol.xcloud.factory.IInstances;
import com.clustercontrol.xcloud.factory.IUserManagement;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudPlatformEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntity;
import com.clustercontrol.xcloud.model.StorageEntity;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_admin;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_facility;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_loginuser;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_scope;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_scope_admin;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_scope_location;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_scope_location_role;
import com.clustercontrol.xcloud.validation.AuthorizingValidator_scope_role;
import com.clustercontrol.xcloud.validation.CustomMethodValidator;
import com.clustercontrol.xcloud.validation.MethodValidator.MethodValidationContext;
import com.clustercontrol.xcloud.validation.ModifiableCloudScope;
import com.clustercontrol.xcloud.validation.ParamHolder;
import com.clustercontrol.xcloud.validation.annotation.CustomMethodValidation;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotEmpty;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.NotNullContainer;
import com.clustercontrol.xcloud.validation.annotation.ParamId;

/**
 * クラウド管理オプション用のエンドポイント
 *
 */
@WebService(serviceName = "CloudEndpointService", portName = "CloudEndpointPort", targetNamespace = "http://xcloud.ws.clustercontrol.com", endpointInterface="com.clustercontrol.ws.xcloud.CloudEndpoint")
public class CloudEndpointImpl implements CloudEndpoint, IWebServiceBase, CloudRoleConstants {
	private interface Configuration {
		// クラウド[コンピュート]ビュー : パワーオン - ジョブ(ノード指定)
		@MethodRestriction.Enable
		String makePowerOnInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : パワーオン - ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makePowerOnInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : パワーオフ - ジョブ(ノード指定)
		@MethodRestriction.Enable
		String makePowerOffInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : パワーオフ - ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makePowerOffInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : 再起動 - ジョブ(ノード指定)
		@MethodRestriction.Enable
		String makeRebootInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : 再起動- ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makeRebootInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : サスペンド - ジョブ(ノード指定)
		@MethodRestriction.Enable
		String makeSuspendInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : サスペンド - ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makeSuspendInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : スナップショット - ジョブ(ノード指定)
		@MethodRestriction.Enable
		String makeSnapshotInstanceCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceId") String instanceId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		
		// クラウド[ストレージ]ビュー : アタッチ - ジョブ
		@MethodRestriction.Enable
		String makeAttachStorageCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceId") String instanceId, @WebParam(name = "storageId") String storageId, @WebParam(name = "options") List<Option> options) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[ストレージ]ビュー : デタッチ - ジョブ
		@MethodRestriction.Enable
		String makeDetachStorageCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageId") String storageId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[ストレージ]ビュー : スナップショット - ジョブ
		@MethodRestriction.Enable
		String makeSnapshotStorageCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageId") String storageId) throws CloudManagerException, InvalidUserPass, InvalidRole;

		// クラウド[コンピュート]ビュー : スコープ割当ルール
		@MethodRestriction.Enable
		void registAutoAssigneNodePattern(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "AutoAssigneNodePatternEntries") List<AutoAssignNodePatternEntry> patterns) throws CloudManagerException, InvalidUserPass, InvalidRole;
		@MethodRestriction.Enable
		List<AutoAssignNodePatternEntry> getAutoAssigneNodePatterns(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		@MethodRestriction.Enable
		void clearAutoAssigneNodePattern(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;
	}
		
	public static final MethodRestriction restriction;
	static {
		restriction = new MethodRestriction(CloudEndpointImpl.class);
		restriction.config(Configuration.class);
	}
	
	public static class MethodRestrictionValidator implements CustomMethodValidator {
		@Override
		public void validate(Method method, ParamHolder params, String group, MethodValidationContext context) throws PluginException {
			String cloudScopeId = params.getParam("XCLOUD_CORE_CLOUDSCOPE_ID", String.class);
			CloudEndpointImpl.internalCheckCallable(cloudScopeId, method);
		}
	}
	
	@Resource
	private WebServiceContext wsctx;
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public CloudPlatform getCloudPlatform(
			@ParamId("XCLOUD_CORE_CLOUDPLATFORM_ID") String cloudPlatformeId) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return new CloudPlatform(CloudManager.singleton().getPlatforms().getCloudPlatform(cloudPlatformeId));
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public List<CloudPlatform> getAllCloudPlatforms() throws CloudManagerException, InvalidUserPass, InvalidRole {
		List<CloudPlatformEntity> entities = CloudManager.singleton().getPlatforms().getAllCloudPlatforms();
		List<CloudPlatform> webEntities = new ArrayList<>();
		for (CloudPlatformEntity entity: entities) {
			webEntities.add(new CloudPlatform(entity));
		}
		return webEntities;
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ})
	@CustomMethodValidation(AuthorizingValidator_admin.class)
	public CloudScope addCloudScope(
			@ParamId("XCLOUD_CORE_ADD_CLOUDSCOPE_REQUEST") @NotNull @Into AddCloudScopeRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return request.transform(new ITransformer<CloudScope>() {
			@Override
			public CloudScope transform(AddPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole {
				return new PublicCloudScope(CloudManager.singleton().getCloudScopes().addPublicCloudScope(request));
			}
			@Override
			public CloudScope transform(AddPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole {
				return new PrivateCloudScope(CloudManager.singleton().getCloudScopes().addPrivateCloudScope(request));
			}
		});
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ})
	@CustomMethodValidation(AuthorizingValidator_admin.class)
	public void removeCloudScope(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull @ModifiableCloudScope String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudManager.singleton().getCloudScopes().removeCloudScope(cloudScopeId);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public CloudScope getCloudScope(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId).transform(new CloudScopeEntity.ITransformer<CloudScope>() {
			@Override
			public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
				return new PublicCloudScope(scope);
			}
			@Override
			public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
				return new PrivateCloudScope(scope);
			}
		});
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_admin.class)
	public List<CloudScope> getAllCloudScopes() throws CloudManagerException, InvalidUserPass, InvalidRole {
		List<CloudScopeEntity> entities = CloudManager.singleton().getCloudScopes().getAllCloudScopes();
		List<CloudScope> webEntities = new ArrayList<>();
		for (CloudScopeEntity entity: entities) {
			webEntities.add(entity.transform(new CloudScopeEntity.ITransformer<CloudScope>() {
				@Override
				public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
					return new PublicCloudScope(scope);
				}
				@Override
				public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
					return new PrivateCloudScope(scope);
				}
			}));
		}
		return webEntities;
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public List<CloudScope> getCloudScopesByRole(
			@ParamId("XCLOUD_CORE_ROLE_ID") @Identity String roleId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		List<CloudLoginUserEntity> users = CloudManager.singleton().getLoginUsers().getCloudLoginUserByRole(roleId);
		Map<String, CloudScope> cloudScopes = new HashMap<>();
		for (CloudLoginUserEntity user: users) {
			if (!cloudScopes.containsKey(user.getCloudScopeId())) {
				cloudScopes.put(user.getCloudScopeId(), user.getCloudScope().transform(new CloudScopeEntity.ITransformer<CloudScope>() {
					@Override
					public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
						return new PublicCloudScope(scope);
					}
					@Override
					public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
						return new PrivateCloudScope(scope);
					}
				}));
			}
		}
		return new ArrayList<>(cloudScopes.values());
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	public CloudLoginUser addCloudLoginUser(
			@ParamId("XCLOUD_CORE_ADD_CLOUDLOGINUSER_REQUEST") @NotNull @Into AddCloudLoginUserRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return new CloudLoginUser(CloudManager.singleton().getLoginUsers().addUser(request));
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public List<CloudLoginUser> getAllCloudLoginUsers(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		List<CloudLoginUserEntity> entities = CloudManager.singleton().getLoginUsers().getCloudLoginUserByCloudScopeAndHinemosUser(cloudScopeId, Session.current().getHinemosCredential().getUserId());
		List<CloudLoginUser> webEntities = new ArrayList<>();
		for (CloudLoginUserEntity entity: entities) {
			webEntities.add(new CloudLoginUser(entity));
		}
		return webEntities;
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_loginuser.class)
	public CloudLoginUser getCloudLoginUser(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId,
			@ParamId("XCLOUD_CORE_CLOUDLOGINUSER_ID") @Identity String cloudLoginUserId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return new CloudLoginUser(CloudManager.singleton().getLoginUsers().getCloudLoginUser(cloudScopeId, cloudLoginUserId));
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@CustomMethodValidation(AuthorizingValidator_loginuser.class)
	public void removeCloudLoginUser(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId,
			@ParamId("XCLOUD_CORE_CLOUDLOGINUSER_ID") @Identity String cloudLoginUserId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudManager.singleton().getLoginUsers().removeCloudLoginUser(cloudScopeId, cloudLoginUserId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	public CloudLoginUser modifyCloudLoginUser(
			@ParamId("XCLOUD_CORE_MODIFY_CLOUDLOGINUSER_REQUEST") @NotNull @Into ModifyCloudLoginUserRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return new CloudLoginUser(CloudManager.singleton().getLoginUsers().modifyCloudLoginUser(request));
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_admin.class)
	public List<PlatformUser> getAvailablePlatformUsers(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
		return scope.optionCall(new CloudScopeEntity.OptionCallable<List<PlatformUser>>() {
			@Override
			public List<PlatformUser> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IUserManagement um = option.getUserManagement(scope);
				final List<PlatformUser> users = new ArrayList<>();
				CollectionComparator.compare(um.getAvailableUsers(), CloudManager.singleton().getLoginUsers().getCloudLoginUserByCloudScope(scope.getId()),
					new CollectionComparator.Comparator<PlatformUser, CloudLoginUserEntity>() {
						@Override
						public boolean match(PlatformUser o1, CloudLoginUserEntity o2) throws CloudManagerException {
							return o1.getCredential().match(o2.getCredential().convertWebElement());
						}
						@Override
						public void afterO1(PlatformUser o1) throws CloudManagerException {
							users.add(o1);
						}
					});
				return users;
			}
		});
	}
	
	@Override
	public void start() {
	}
	
	@Override
	public void stop() {
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	@CustomMethodValidation(AuthorizingValidator_scope_admin.class)
	public void modifyCloudLoginUserPriority(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_CLOUDLOGINUSER_IDS") @NotNull List<String> cloudLoginUserIds) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudManager.singleton().getLoginUsers().modifyCloudLoginUserPriority(cloudScopeId, cloudLoginUserIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	@CustomMethodValidation(AuthorizingValidator_scope_admin.class)
	public CloudLoginUser modifyCloudLoginUserRoleRelation(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_CLOUDLOGINUSER_ID") @NotNull String cloudLoginUserId,
			@ParamId("XCLOUD_CORE_ROLERELATIONS") @NotNull List<RoleRelation> roleRelations)
			throws CloudManagerException, InvalidUserPass, InvalidRole {
		return new CloudLoginUser(CloudManager.singleton().getLoginUsers().modifyRoleRelation(cloudScopeId, cloudLoginUserId, roleRelations));
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_admin.class)
	public List<String> getCloudLoginUserPriority(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getLoginUsers().getCloudLoginUserPriority(cloudScopeId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	public CloudScope modifyCloudScope(
			@ParamId("XCLOUD_CORE_MODIFY_CLOUDSCOPE_REQUEST") @NotNull ModifyCloudScopeRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getCloudScopes().modifyCloudScope(request).transform(new CloudScopeEntity.ITransformer<CloudScope>() {
			@Override
			public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
				return new PublicCloudScope(scope);
			}
			@Override
			public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
				return new PrivateCloudScope(scope);
			}
		});
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void removeInstances(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).removeInstances(instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<Instance> getAllInstances(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		List<Instance> instances = new ArrayList<>();
		try {
			ActionMode.enterAutoDetection();
			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<InstanceEntity> instanceEntities = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).updateInstances(new ArrayList<String>());
			for (InstanceEntity entity: instanceEntities) {
				instances.add(new Instance(entity));
			}
		} finally {
			ActionMode.leaveAutoDetection();;
		}
		return instances;
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<Instance> getInstances(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		List<Instance> instances = new ArrayList<>();
		try {
			ActionMode.enterAutoDetection();
			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<InstanceEntity> instanceEntities = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).updateInstances(instanceIds);
			for (InstanceEntity entity: instanceEntities) {
				instances.add(new Instance(entity));
			}
		} finally {
			ActionMode.leaveAutoDetection();;
		}
		return instances;
	}
	
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<InstanceBackup> getInstanceBackups(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		List<InstanceBackupEntity> entities = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).updateInstanceBackups(instanceIds);
		return InstanceBackup.convertWebEntities(entities);
	}
	
	protected interface InstancesExecutor {
		void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException;
		void throwException(List<String> failedLocations) throws CloudManagerException;
	}
	
	protected List<Instance> executeInstancesOperationUsingFacility(String cloudScopeId, String facilityId, InstancesExecutor executor) throws CloudManagerException, InvalidUserPass, InvalidRole {
		Map<String, List<String>> locationMap = new HashMap<>();
		List<InstanceEntity> queryResults;
		try {
			List<String> facilityIds = new ArrayList<>();
			facilityIds.addAll(RepositoryControllerBeanWrapper.bean().getFacilityIdList(facilityId, RepositoryControllerBean.ALL , false));
			if (facilityIds.isEmpty())
				return Collections.emptyList();
			
			HinemosEntityManager em = Session.current().getEntityManager();
			
			TypedQuery<InstanceEntity> query = em.createNamedQuery(InstanceEntity.findInstancesByFacilityIds, InstanceEntity.class);
			query.setParameter("cloudScopeId", cloudScopeId);
			query.setParameter("facilityIds", facilityIds);
			
			queryResults = query.getResultList();
			
			for (InstanceEntity instanceEntiy: queryResults) {
				List<String> list = locationMap.get(instanceEntiy.getLocationId());
				if (list == null) {
					list = new ArrayList<>();
					locationMap.put(instanceEntiy.getLocationId(), list);
				}
				list.add(instanceEntiy.getResourceId());
			}
		} catch (HinemosUnknown e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}
		
		List<String> failedLocations = new ArrayList<>();
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		for (Map.Entry<String, List<String>> entry: locationMap.entrySet()) {
			try {
				IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(entry.getKey()));
				executor.execute(instances, entry.getValue());
			} catch (CloudManagerException e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
				failedLocations.add(entry.getKey());
			}
		}
		if (!failedLocations.isEmpty())
			executor.throwException(failedLocations);
		
		List<Instance> instances = new ArrayList<>();
		for (InstanceEntity entity: queryResults) {
			instances.add(new Instance(entity));
		}
		return instances;
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void powerOnInstances(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).powerOnInstances(instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public List<Instance> powerOnInstancesUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull final String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull final String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return executeInstancesOperationUsingFacility(cloudScopeId, facilityId,
				new InstancesExecutor() {
					@Override
					public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
						instances.powerOnInstances(instanceIds);
					}
					@Override
					public void throwException(List<String> failedLocations) throws CloudManagerException {
						throw ErrorCode.CLOUDINSTANCE_FIAL_TO_POWERON_INSTANCE_BY_FACILITY.cloudManagerFault(cloudScopeId, facilityId, failedLocations.toString());
					}
				});
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makePowerOnInstancesCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull @NotEmpty List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlServerCommand("ServerPowerOn.py", cloudScopeId, locationId, instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope.class, MethodRestrictionValidator.class})
	public String makePowerOnInstancesCommandUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlCommandUsingFacility("ServerPowerOnUsingFacility.py", cloudScopeId, facilityId);
	}

	protected String getAutoContorlServerCommand(String scriptName, String cloudScopeId, String locationId, List<String> instanceIds) throws CloudManagerException {
		String scriptPath;
		String hinemosHome = System.getProperty("hinemos.manager.home.dir");
		if (Boolean.valueOf(System.getProperty("CloudDevMode", Boolean.FALSE.toString()))) {
			String path = System.getProperty("user.dir");
			scriptPath = "python.exe " + path + (path.endsWith("\\") ? "": "\\") + "settings\\sbin\\xcloud\\" + scriptName;
		} else {
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = hinemosHome + (hinemosHome.endsWith("\\") ? "": "\\") + CloudConstants.PATH_SBIN.replace('/', '\\') + "\\" + scriptName;
			} else {
				scriptPath = hinemosHome + (hinemosHome.endsWith("/") ? "": "/") + CloudConstants.PATH_SBIN + "/" + scriptName;
			}
			
			if (!new File(scriptPath).exists())
				throw ErrorCode.AUTO_CONTROL_NOT_FOUND_SCRIPT.cloudManagerFault(scriptPath);
			
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = "\"" + scriptPath + "\"";
			} else {
				scriptPath = scriptPath.replace(" ", "\\ ");
			}
		}
		
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		
		List<InstanceEntity> instanceEntities;
		try {
			ActionMode.enterAutoDetection();
			instanceEntities = new ArrayList<>(CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).updateInstances(instanceIds));
		} finally {
			ActionMode.leaveAutoDetection();
		}
		
		StringBuilder sb = new StringBuilder();
		for (String instanceId: instanceIds) {
			boolean matched = false;
			Iterator<InstanceEntity> iter = instanceEntities.iterator();
			while (iter.hasNext()) {
				InstanceEntity entity = iter.next();
				if (entity.getResourceId().equals(instanceId)) {
					matched = true;
					iter.remove();
					break;
				}
			}
			if (!matched)
				throw ErrorCode.AUTO_CONTROL_NOT_FOUND_INSTANCE.cloudManagerFault(instanceId);
			
			sb.append(instanceId + " ");
		}
		
		return String.format("%s%s -u %s -s %s -l %s -i \"%s\"",
				"".equals(HinemosPropertyCommon.xcloud_env_path_python.getStringValue()) ? "" : HinemosPropertyCommon.xcloud_env_path_python.getStringValue() + " ",
				scriptPath,
				Session.current().getHinemosCredential().getUserId(),
				cloudScopeId,
				locationId,
				sb.toString().trim()
				);
	}
	
	protected String getAutoContorlStorageCommand(String scriptName, String cloudScopeId, String locationId, List<String> storageIds) throws CloudManagerException {
		String scriptPath;
		if (Boolean.valueOf(System.getProperty("CloudDevMode", Boolean.FALSE.toString()))) {
			String path = System.getProperty("user.dir");
			scriptPath = "python.exe " + path + (path.endsWith("\\") ? "": "\\") + "settings\\sbin\\xcloud\\" + scriptName;
		} else {
			String hinemosHome = System.getProperty("hinemos.manager.home.dir");
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = hinemosHome + (hinemosHome.endsWith("\\") ? "": "\\") + CloudConstants.PATH_SBIN.replace('/', '\\') + "\\" + scriptName;
			} else {
				scriptPath = hinemosHome + (hinemosHome.endsWith("/") ? "": "/") + CloudConstants.PATH_SBIN + "/" + scriptName;
			}

			if (!new File(scriptPath).exists())
				throw ErrorCode.AUTO_CONTROL_NOT_FOUND_SCRIPT.cloudManagerFault(scriptPath);
			
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = "\"" + scriptPath + "\"";
			} else {
				scriptPath = scriptPath.replace(" ", "\\ ");
			}
		}
		
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		List<StorageEntity> storageEntities = new ArrayList<>(CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).updateStorages(storageIds));
		
		StringBuilder sb = new StringBuilder();
		for (String storageId: storageIds) {
			boolean matched = false;
			Iterator<StorageEntity> iter = storageEntities.iterator();
			while (iter.hasNext()) {
				StorageEntity entity = iter.next();
				if (entity.getResourceId().equals(storageId)) {
					matched = true;
					iter.remove();
					break;
				}
			}
			if (!matched){
				throw ErrorCode.AUTO_CONTROL_NOT_FOUND_STORAGE.cloudManagerFault(storageId);
			}
			sb.append(storageId + " ");
		}
		
		return String.format("%s%s -u %s -s %s -l %s -t \"%s\"",
				"".equals(HinemosPropertyCommon.xcloud_env_path_python.getStringValue()) ? "" : HinemosPropertyCommon.xcloud_env_path_python.getStringValue() + " ", 
				scriptPath,
				Session.current().getHinemosCredential().getUserId(),
				cloudScopeId,
				locationId,
				sb.toString().trim()
				);
	}
	
	protected String getAutoContorlCommandUsingFacility(String scriptName, String cloudScopeId, String facilityId) throws CloudManagerException {
		String hinemosHome = System.getProperty("hinemos.manager.home.dir");
		String scriptPath;
		if (Boolean.valueOf(System.getProperty("CloudDevMode", Boolean.FALSE.toString()))) {
			String path = System.getProperty("user.dir");
			scriptPath = path + (path.endsWith("\\") ? "": "\\") + "settings\\sbin\\xcloud\\" + "StorageAttach.py";
		} else {
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = hinemosHome + (hinemosHome.endsWith("\\") ? "": "\\") + CloudConstants.PATH_SBIN.replace('/', '\\') + "\\" + scriptName;
			} else {
				scriptPath = hinemosHome + (hinemosHome.endsWith("/") ? "": "/") + CloudConstants.PATH_SBIN + "/" + scriptName;
			}
			
			if (!new File(scriptPath).exists())
				throw ErrorCode.AUTO_CONTROL_NOT_FOUND_SCRIPT.cloudManagerFault(scriptPath);
			
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = "\"" + scriptPath + "\"";
			} else {
				scriptPath = scriptPath.replace(" ", "\\ ");
			}
		}
		
		return String.format("%s%s -u %s -s %s -f %s",
				"".equals(HinemosPropertyCommon.xcloud_env_path_python.getStringValue()) ? "" : HinemosPropertyCommon.xcloud_env_path_python.getStringValue() + " ",
				scriptPath,
				Session.current().getHinemosCredential().getUserId(),
				cloudScopeId,
				facilityId
				);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void rebootInstances(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).rebootInstances(instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public List<Instance> rebootInstancesUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull final String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull final String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return executeInstancesOperationUsingFacility(cloudScopeId, facilityId,
				new InstancesExecutor() {
					@Override
					public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
						instances.rebootInstances(instanceIds);
					}
					@Override
					public void throwException(List<String> failedLocations) throws CloudManagerException {
						throw ErrorCode.CLOUDINSTANCE_FIAL_TO_REBOOT_INSTANCE_BY_FACILITY.cloudManagerFault(cloudScopeId, facilityId, failedLocations.toString());
					}
				});
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makeRebootInstancesCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlServerCommand("ServerReboot.py", cloudScopeId, locationId, instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope.class, MethodRestrictionValidator.class})
	public String makeRebootInstancesCommandUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlCommandUsingFacility("ServerRebootUsingFacility.py", cloudScopeId, facilityId);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void powerOffInstances(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).powerOffInstances(instanceIds);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public List<Instance> powerOffInstancesUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull final String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull final String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return executeInstancesOperationUsingFacility(cloudScopeId, facilityId,
				new InstancesExecutor() {
					@Override
					public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
						instances.powerOffInstances(instanceIds);
					}
					@Override
					public void throwException(List<String> failedLocations) throws CloudManagerException {
						throw ErrorCode.CLOUDINSTANCE_FIAL_TO_POWEROFF_INSTANCE_BY_FACILITY.cloudManagerFault(cloudScopeId, facilityId, failedLocations.toString());
					}
				});
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makePowerOffInstancesCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlServerCommand("ServerPowerOff.py", cloudScopeId, locationId, instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope.class, MethodRestrictionValidator.class})
	public String makePowerOffInstancesCommandUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlCommandUsingFacility("ServerPowerOffUsingFacility.py", cloudScopeId, facilityId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void suspendInstances(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).suspendInstances(instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public List<Instance> suspendInstancesUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull final String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull final String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return executeInstancesOperationUsingFacility(cloudScopeId, facilityId,
				new InstancesExecutor() {
					@Override
					public void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException {
						instances.suspendInstances(instanceIds);
					}
					@Override
					public void throwException(List<String> failedLocations) throws CloudManagerException {
						throw ErrorCode.CLOUDINSTANCE_FIAL_TO_SUSPEND_INSTANCE_BY_FACILITY.cloudManagerFault(cloudScopeId, facilityId, failedLocations.toString());
					}
				});
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makeSuspendInstancesCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_IDS") @NotNullContainer @NotNull List<String> instanceIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlServerCommand("ServerSuspend.py", cloudScopeId, locationId, instanceIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope.class, MethodRestrictionValidator.class})
	public String makeSuspendInstancesCommandUsingFacility(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_FACILITY_ID") @NotNull String facilityId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlCommandUsingFacility("ServerSuspendUsingFacility.py", cloudScopeId, facilityId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public InstanceBackup snapshotInstance(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_CREATE_INSTANCESNAPSHOT_REQUEST") @NotNull @Into CreateInstanceSnapshotRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		InstanceBackupEntity backup = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).takeInstanceSnapshot(request.getInstanceId(), request.getName(), request.getDescription(), request.getOptions());
		return InstanceBackup.convertWebEntity(backup);
	}


	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makeSnapshotInstanceCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_ID") @NotNullContainer @NotNull String instanceId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlServerCommand("ServerSnapshot.py", cloudScopeId, locationId, Arrays.asList(instanceId));
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void deleteInstanceSnapshots(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_ID") String isntanceId,
			@ParamId("XCLOUD_CORE_INSTANCESNAPSHOT_IDS") List<String> instanceSnapshotIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).deletInstanceSnapshots(isntanceId, instanceSnapshotIds);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public Instance cloneBackupedInstance(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") String locationId,
			@ParamId("XCLOUD_CORE_CLONE_BACKUPEDINSTANCE_REQUEST") CloneBackupedInstanceRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		InstanceEntity instanceEntity = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).cloneBackupedInstance(request);
		return Instance.convertWebEntity(instanceEntity);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void removeStorages(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_STORAGE_IDS") @NotNullContainer @NotNull List<String> storageIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).removeStorages(storageIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<Storage> getStorages(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_STORAGE_IDS") @NotNullContainer @NotNull List<String> storageIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		List<StorageEntity> storages = CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).updateStorages(storageIds);
		return Storage.convertWebEntities(storages);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<Storage> getAllStorages(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		List<StorageEntity> storages = CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).updateStorages(CloudUtil.emptyList(String.class));
		return Storage.convertWebEntities(storages);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public HRepository getRepository() throws CloudManagerException {
		return CloudManager.singleton().getRepository().getRepository();
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public HRepository getRepositoryByRole(
			@ParamId("XCLOUD_CORE_ROLE_ID") @NotNull String roleId
			) throws CloudManagerException {
		return CloudManager.singleton().getRepository().getRepository(roleId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public HRepository updateLocationRepository(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId
			) throws CloudManagerException {
		try {
			ActionMode.enterAutoDetection();
			return CloudManager.singleton().getRepository().updateLocationRepository(cloudScopeId, locationId);
		} finally {
			ActionMode.leaveAutoDetection();
		}
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public List<PlatformServiceCondition> getPlatformServiceConditions(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeId);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_role.class)
	public List<PlatformServiceCondition> getPlatformServiceConditionsByRole(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_ROLE_ID") @NotNull String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<PlatformServiceCondition> getPlatformServiceConditionsByLocation(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeId, locationId);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location_role.class)
	public List<PlatformServiceCondition> getPlatformServiceConditionsByLocationAndRole(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_ROLE_ID") @NotNull String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeId, locationId);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public String getVersion() throws CloudManagerException, InvalidUserPass, InvalidRole {
		String version = KeyCheck.getResultXcloud();
		Logger.getLogger(this.getClass()).debug(version);
		boolean result = Boolean.valueOf(version.substring(7, version.length()));
		if(!result) {
			throw new CloudManagerException("expiration of a term", ErrorCode.UNEXPECTED.getMessage());
		}
		return VersionUtil.getVersion();
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public Instance modifyInstance(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_MODIFY_INSTANCE_REQUEST") @NotNull @Into ModifyInstanceRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		IInstances instances = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId));
		return new Instance(instances.modifyInstance(request));
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	@CustomMethodValidation({AuthorizingValidator_scope_admin.class, MethodRestrictionValidator.class})
	public void registAutoAssigneNodePattern(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull @Identity String cloudScopeId,
			@ParamId("XCLOUD_CORE_AUTOASSIGNE_NODEPATTERN_ENTRIES") @NotNull @Into List<AutoAssignNodePatternEntry> patterns) throws CloudManagerException, InvalidUserPass, InvalidRole {
		ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
		scopes.registAutoAssigneNodePattern(cloudScopeId, patterns);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation({AuthorizingValidator_scope.class, MethodRestrictionValidator.class})
	public List<AutoAssignNodePatternEntry> getAutoAssigneNodePatterns(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull @Identity String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
		return AutoAssignNodePatternEntry.convertWebEntities(scopes.getAutoAssigneNodePatterns(cloudScopeId));
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	@CustomMethodValidation({AuthorizingValidator_scope.class, MethodRestrictionValidator.class})
	public void clearAutoAssigneNodePattern(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
		scopes.clearAutoAssigneNodePattern(cloudScopeId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makeAttachStorageCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_ID") @NotNull String instanceId,
			@ParamId("XCLOUD_CORE_STORAGE_ID") @NotNull String storageId,
			@ParamId("XCLOUD_CORE_OPTIONS") List<Option> options) throws CloudManagerException, InvalidUserPass, InvalidRole {
		String hinemosHome = System.getProperty("hinemos.manager.home.dir");
		String scriptPath;
		if (Boolean.valueOf(System.getProperty("CloudDevMode", Boolean.FALSE.toString()))) {
			String path = System.getProperty("user.dir");
			scriptPath = "python.exe " + path + (path.endsWith("\\") ? "": "\\") + "settings\\sbin\\xcloud\\" + "StorageAttach.py";
		} else {
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = hinemosHome + (hinemosHome.endsWith("\\") ? "": "\\") + CloudConstants.PATH_SBIN.replace('/', '\\') + "\\" + "StorageAttach.py";
			} else {
				scriptPath = hinemosHome + (hinemosHome.endsWith("/") ? "": "/") + CloudConstants.PATH_SBIN + "/" + "StorageAttach.py";
			}
			
			if (!new File(scriptPath).exists())
				throw ErrorCode.AUTO_CONTROL_NOT_FOUND_SCRIPT.cloudManagerFault(scriptPath);
			
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				scriptPath = "\"" + scriptPath + "\"";
			} else {
				scriptPath = scriptPath.replace(" ", "\\ ");
			}
		}
		
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).getInstance(instanceId);
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).getStorage(storageId);
		
		String command = String.format("%s%s -u %s -s %s -l %s -i \"%s\" -t \"%s\"",
				"".equals(HinemosPropertyCommon.xcloud_env_path_python.getStringValue()) ? "" : HinemosPropertyCommon.xcloud_env_path_python.getStringValue() + " ",
				scriptPath,
				Session.current().getHinemosCredential().getUserId(),
				cloudScopeId,
				locationId,
				instanceId,
				storageId
				);
		
		if (!options.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"{");
			for (int i = 0; i < options.size(); ++i) {
				Option o = options.get(i);
				sb.append("'");
				sb.append(o.getName());
				sb.append("':'");
				sb.append(o.getValue());
				sb.append("'");
				
				if (i < options.size() - 1)
					sb.append(",");
			}
			sb.append("}\"");
			
			command = command + " -o " + sb.toString();
		}
		return command;
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void attachStorage(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_ID") @NotNull String instanceId,
			@ParamId("XCLOUD_CORE_STORAGE_ID") @NotNull String storageId,
			@ParamId("XCLOUD_CORE_OPTIONS") @NotNull List<Option> options) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).attachStorage(instanceId, storageId, options);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makeDetachStorageCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_STORAGE_ID") @NotNull String storageId) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlStorageCommand("StorageDetach.py", cloudScopeId, locationId, Arrays.asList(storageId));
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.MODIFY)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void detachStorage(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_STORAGE_IDS") @NotNull List<String> storageIds) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).detachStorage(storageIds);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public List<String> getManagerFacilityIds() throws CloudManagerException, InvalidUserPass, InvalidRole {
		String hostName = System.getProperty("hinemos.manager.hostname");

		Set<String> facilityIdSet = RepositoryControllerBeanWrapper.bean().getNodeListByHostname(hostName);
		if (facilityIdSet == null)
			return Collections.emptyList();
		
		List<String> facilityIds = new ArrayList<>();
		for (String facilityId: facilityIdSet) {
			AgentInfo agent = AgentConnectUtil.getAgentInfo(facilityId);
			if (agent != null) {
				facilityIds.add(facilityId);
			}
		}
		
		return facilityIds;
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<StorageBackup> getStorageBackups(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_STORAGE_IDS") @NotNullContainer @NotNull final List<String> storageIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		List<StorageBackupEntity> entities = CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).updateStorageBackups(storageIds);
		return StorageBackup.convertWebEntities(entities);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public StorageBackup snapshotStorage(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @NotNull String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @NotNull String locationId,
			@ParamId("XCLOUD_CORE_CREATE_STORAGESNAPSHOT_REQUEST") @NotNull @Into CreateStorageSnapshotRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		StorageBackupEntity backup = CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).takeStorageSnapshot(request.getStorageId(), request.getName(), request.getDescription(), request.getOptions());
		return StorageBackup.convertWebEntity(backup);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation({AuthorizingValidator_scope_location.class, MethodRestrictionValidator.class})
	public String makeSnapshotStorageCommand(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") String locationId,
			@ParamId("XCLOUD_CORE_STORAGE_ID") String storageId)
			throws CloudManagerException, InvalidUserPass, InvalidRole {
		return getAutoContorlStorageCommand("StorageSnapshot.py", cloudScopeId, locationId, Arrays.asList(storageId));
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void deleteStorageSnapshots(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") String locationId,
			@ParamId("XCLOUD_CORE_STORAGE_ID") String storageId,
			@ParamId("XCLOUD_CORE_STORAGESNAPSHOT_IDS") List<String> storageSnapshotIds
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).deletStorageSnapshots(storageId, storageSnapshotIds);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.EXEC)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public Storage cloneBackupedStorage(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") String locationId,
			@ParamId("XCLOUD_CORE_CLONE_BACKUPEDSTORAGE_REQUEST") CloneBackupedStorageRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		StorageEntity storageEntity = CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId)).cloneBackupedStorage(request);
		return Storage.convertWebEntity(storageEntity);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public List<Network> getAllNetworks(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") String locationId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		return CloudManager.singleton().getNetworks(user, user.getCloudScope().getLocation(locationId)).getAllNetwork();
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public BillingResult getBillingDetailsByCloudScope(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId,
			@ParamId("XCLOUD_CORE_YEAR") @NotNull int year,
			@ParamId("XCLOUD_CORE_MONTH") @NotNull int month
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getBillings().getBillingDetailsByCloudScope(cloudScopeId, year, month);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	@CustomMethodValidation(AuthorizingValidator_facility.class)
	public BillingResult getBillingDetailsByFacility(
			@ParamId("XCLOUD_CORE_FACILITY_ID") @Identity String facilityId,
			@ParamId("XCLOUD_CORE_YEAR") @NotNull int year,
			@ParamId("XCLOUD_CORE_MONTH") @NotNull int month
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getBillings().getBillingDetailsByFacility(facilityId, year, month);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ})
	@CustomMethodValidation(AuthorizingValidator_scope.class)
	public DataHandler downloadBillingDetailsByCloudScope(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId,
			@ParamId("XCLOUD_CORE_YEAR") @NotNull int year,
			@ParamId("XCLOUD_CORE_MONTH") @NotNull int month
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getBillings().downloadBillingDetailsByCloudScope(cloudScopeId, year, month);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ})
	@CustomMethodValidation(AuthorizingValidator_facility.class)
	public DataHandler downloadBillingDetailsByFacility(
			@ParamId("XCLOUD_CORE_FACILITY_ID") @Identity String facilityId,
			@ParamId("XCLOUD_CORE_YEAR") @NotNull int year,
			@ParamId("XCLOUD_CORE_MONTH") @NotNull int month
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getBillings().downloadBillingDetailsByFacility(facilityId, year, month);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.MODIFY})
	public void modifyBillingSetting(
			@ParamId("XCLOUD_CORE_MODIFY_BILLINGSETTING_REQUEST") @NotNull @Into ModifyBillingSettingRequest request
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudManager.singleton().getCloudScopes().modifyBillingSetting(request);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.MODIFY})
	public void refreshBillingDetails(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudManager.singleton().getBillings().refreshBillingDetails(cloudScopeId);
	}

	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ})
	public List<String> getPlatformServices(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId
			) throws CloudManagerException, InvalidUserPass, InvalidRole {
		return CloudManager.singleton().getBillings().getPlatformServices(cloudScopeId);
	}
	
	/**
	 * プラットフォームのサービス一覧を取得する。
	 * @param facilityId
	 * @param RoleId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ})
	public List<PlatformServices> getAvailablePlatformServices(
			@ParamId("XCLOUD_CORE_FACILITY_ID") @Identity String facilityId,
			@ParamId("XCLOUD_CORE_ROLE_ID") @NotNull String roleId
			) throws CloudManagerException, InvalidUserPass, InvalidRole, FacilityNotFound, HinemosUnknown {
		List<String> nodeFacilities;
		if (RepositoryControllerBeanWrapper.bean().isNode(facilityId)){
			nodeFacilities = Arrays.asList(facilityId);
		} else {
			nodeFacilities = RepositoryControllerBeanWrapper.bean().getNodeFacilityIdList(facilityId, roleId, 0);//0は配下すべて
		}
		
		CloudManager cManager = CloudManager.singleton();
		List<CloudScopeEntity> cloudScopes = cManager.getCloudScopes().getCloudScopesByCurrentHinemosUser();
		Set<PlatformServices> platiformServices = new HashSet<>();
		for (CloudScopeEntity entity : cloudScopes) {
			String nodeId = FacilityIdUtil.getCloudScopeNodeId(entity.getPlatformId(), entity.getId());
			if (nodeFacilities.contains(nodeId)) {
				List<String> services = cManager.getBillings().getPlatformServices(entity.getId());
				for (String service : services) {
					PlatformServices platiformService = new PlatformServices();
					platiformService.setPlatformId(entity.getPlatformId());
					platiformService.setServiceId(service);
					platiformServices.add(platiformService);
				}
			}
		}
		return new ArrayList<>(platiformServices);
	}
	
	/**
	 * プラットフォームのサービス一覧を取得する。
	 * @param facilityId
	 * @param RoleId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.READ})
	public List<PlatformServices> getAvailablePlatformServicesByUnlimited(
			@ParamId("XCLOUD_CORE_FACILITY_ID") @Identity String facilityId,
			@ParamId("XCLOUD_CORE_ROLE_ID") @NotNull String roleId
			) throws CloudManagerException, InvalidUserPass, InvalidRole, FacilityNotFound, HinemosUnknown {
		List<String> nodeFacilities;
		if (RepositoryControllerBeanWrapper.bean().isNode(facilityId)){
			nodeFacilities = Arrays.asList(facilityId);
		} else {
			nodeFacilities = RepositoryControllerBeanWrapper.bean().getNodeFacilityIdList(facilityId, roleId, 0);//0は配下すべて
		}
		
		Map<String, CloudScopeEntity> cloudScopes = new HashMap<>();
		if ("ADMINISTRATORS".equals(roleId)) {
			CloudManager.singleton().getCloudScopes().getAllCloudScopes().stream().forEach(s->cloudScopes.put(s.getCloudScopeId(), s));
		} else {
			List<CloudLoginUserEntity> users = CloudManager.singleton().getLoginUsers().getCloudLoginUserByRole(roleId);
			for (CloudLoginUserEntity user: users) {
				if (!cloudScopes.containsKey(user.getCloudScopeId())) {
					cloudScopes.put(user.getCloudScopeId(), user.getCloudScope());
				}
			}
		}
		
		CloudManager cManager = CloudManager.singleton();
		Set<PlatformServices> platiformServices = new HashSet<>();
		for (CloudScopeEntity entity : cloudScopes.values()) {
			String nodeId = FacilityIdUtil.getCloudScopeNodeId(entity.getPlatformId(), entity.getId());
			if (nodeFacilities.contains(nodeId)) {
				List<String> services = cManager.getBillings().getPlatformServices(entity.getId());
				for (String service : services) {
					PlatformServices platiformService = new PlatformServices();
					platiformService.setPlatformId(entity.getPlatformId());
					platiformService.setServiceId(service);
					platiformServices.add(platiformService);
				}
			}
		}
		return new ArrayList<>(platiformServices);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right={SystemPrivilegeMode.MODIFY})
	@CustomMethodValidation(AuthorizingValidator_scope_location.class)
	public void assignNodeToInstance(
			@ParamId("XCLOUD_CORE_CLOUDSCOPE_ID") @Identity String cloudScopeId,
			@ParamId("XCLOUD_CORE_LOCATION_ID") @Identity String locationId,
			@ParamId("XCLOUD_CORE_INSTANCE_ID") String instanceId) throws CloudManagerException, InvalidUserPass, InvalidRole {
		CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getPrimaryCloudLoginUserByCurrent(cloudScopeId);
		CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId)).assignNode(instanceId);
	}
	
	@Override
	@HinemosAccessRight(roleName=CLOUDMANAGEMENT, right=SystemPrivilegeMode.READ)
	public List<AvailableRole> getAvailableRoles() throws CloudManagerException, InvalidUserPass, InvalidRole {
		TypedQuery<RoleInfo> query = null;
		List<String> excludes = Arrays.asList(RoleIdConstant.INTERNAL, RoleIdConstant.HINEMOS_MODULE);
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator == null || !isAdministrator) {
			HinemosEntityManager em = Session.current().getEntityManager();
			query = em.createNamedQuery(CloudScopeEntity.findAvailableRoles, RoleInfo.class);
			query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
			query.setParameter("excludes", excludes);
		} else {
			HinemosEntityManager em = Session.current().getEntityManager();
			query = em.createNamedQuery(CloudScopeEntity.findAvailableRolesAsAdmin, RoleInfo.class);
			query.setParameter("excludes", excludes);
		}
		
		List<AvailableRole> availableRoles = new ArrayList<>();
		List<RoleInfo> roles = query.getResultList();
		for (RoleInfo r: roles) {
			availableRoles.add(new AvailableRole(r.getRoleId(), r.getRoleName()));
		}
		return availableRoles;
	}

	@Override
	public void checkCallable(String cloudScopeId, String methodName) throws CloudManagerException, InvalidUserPass, InvalidRole {
		Method method = null;
		for (Method m: this.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				method = m;
				break;
			}
		}
		
		if (method == null)
			throw ErrorCode.COMMUNITY_EDITION_FUNC_NOT_AVAILABLE.cloudManagerFault(this.getClass().getSimpleName(), methodName);
		
		internalCheckCallable(cloudScopeId, method);
	}

	public static void internalCheckCallable(String cloudScopeId, final Method method) throws CloudManagerException {
		CloudScopeEntity cloudScope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
		MethodRestriction.Status optionStatus = cloudScope.optionCall(new CloudScopeEntity.OptionCallable<MethodRestriction.Status>() {
			@Override
			public MethodRestriction.Status call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				return option.getMethodRestriction(method.getClass()).check(method);
			}
		});
		
		switch(optionStatus) {
		case enable:
			return;
		case disable:
			throw ErrorCode.COMMUNITY_EDITION_FUNC_NOT_AVAILABLE.cloudManagerFault(method.getDeclaringClass().getSimpleName(), method.getName());
		case none:
			switch(CloudEndpointImpl.restriction.check(method)) {
			case enable:
				return;
			case disable:
				throw ErrorCode.COMMUNITY_EDITION_FUNC_NOT_AVAILABLE.cloudManagerFault(method.getDeclaringClass().getSimpleName(), method.getName());
			case none:
				return;
			}
		}
	}
}
