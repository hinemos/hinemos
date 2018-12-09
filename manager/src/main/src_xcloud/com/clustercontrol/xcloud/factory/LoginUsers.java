/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import static com.clustercontrol.xcloud.common.CloudConstants.Event_LoginUser;
import static com.clustercontrol.xcloud.common.CloudConstants.Event_LoginUserAccount;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AccessKeyCredential;
import com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.Credential;
import com.clustercontrol.xcloud.bean.Filter;
import com.clustercontrol.xcloud.bean.ModifyCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.RoleRelation;
import com.clustercontrol.xcloud.bean.UserCredential;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager.OptionExecutor;
import com.clustercontrol.xcloud.model.AccessKeyCredentialEntity;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CredentialBaseEntity;
import com.clustercontrol.xcloud.model.RoleRelationEntity;
import com.clustercontrol.xcloud.model.UserCredentialEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.AccessControllerBeanWrapper;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.CloudUtil;

@Transactional
public class LoginUsers implements ILoginUsers {
	
	private static class CredentialTransformer implements Credential.ITransformer<CredentialBaseEntity> {
		private String cloudScopeId;
		private String loginUserId;
		
		public CredentialTransformer(String cloudScopeId, String loginUserId) {
			this.cloudScopeId = cloudScopeId;
			this.loginUserId = loginUserId;
		}
		
		@Override
		public CredentialBaseEntity transform(AccessKeyCredential credential) throws CloudManagerException {
			AccessKeyCredentialEntity entity = new AccessKeyCredentialEntity();
			entity.setCloudScopeId(cloudScopeId);
			entity.setLoginUserId(loginUserId);
			entity.setAccessKey(credential.getAccessKey());
			entity.setSecretKey(credential.getSecretKey());
			return entity;
		}

		@Override
		public CredentialBaseEntity transform(UserCredential credential) throws CloudManagerException {
			UserCredentialEntity entity = new UserCredentialEntity();
			entity.setCloudScopeId(cloudScopeId);
			entity.setLoginUserId(loginUserId);
			entity.setUser(credential.getUser());
			entity.setPassword(credential.getPassword());
			return entity;
		}
	};
	
	public LoginUsers() {
	}

	@Override
	public CloudLoginUserEntity addAccount(AddCloudLoginUserRequest request) throws CloudManagerException {
		CloudLoginUserEntity user = createInternalCloudUser(request, CloudLoginUserEntity.CloudUserType.account);
		try {
			try (AddedEventNotifier<CloudLoginUserEntity> notifire = new AddedEventNotifier<CloudLoginUserEntity>(CloudLoginUserEntity.class, Event_LoginUserAccount, user)) {
				HinemosEntityManager em = Session.current().getEntityManager();
				PersistenceUtil.persist(em, user);
				em.flush();

				Query query = em.createNamedQuery("updateAccountOnCloudScope");
				query.setParameter("account", user);
				query.setParameter("cloudScopeId", request.getCloudScopeId());
				query.executeUpdate();
				
				CloudScopeEntity scope = em.find(CloudScopeEntity.class, request.getCloudScopeId(), ObjectPrivilegeMode.READ);
				scope.setAccountId(user.getLoginUserId());
				scope.setAccount(user);
				
				decoraiteUserLogin(user, request);
				
				notifire.setCompleted();
			}

			return user;
		}
		catch (EntityExistsException e) {
			throw ErrorCode.LOGINUSER_ALREADY_EXIST.cloudManagerFault(user.getLoginUserId());
		}
	}

	@Override
	public CloudLoginUserEntity addUser(AddCloudLoginUserRequest request) throws CloudManagerException {
		CloudLoginUserEntity user = createInternalCloudUser(request, CloudLoginUserEntity.CloudUserType.user);
		try {
			try (AddedEventNotifier<CloudLoginUserEntity> notifire = new AddedEventNotifier<CloudLoginUserEntity>(CloudLoginUserEntity.class, Event_LoginUser, user)) {
				HinemosEntityManager em = Session.current().getEntityManager();
				PersistenceUtil.persist(em, user);
				em.flush();
				decoraiteUserLogin(user, request);
			}
			return user;
		}
		catch (EntityExistsException e) {
			throw ErrorCode.LOGINUSER_ALREADY_EXIST.cloudManagerFault(user.getLoginUserId());
		}
	}

	private CloudLoginUserEntity createInternalCloudUser(final AddCloudLoginUserRequest request, CloudLoginUserEntity.CloudUserType type) throws CloudManagerException {
		final CloudLoginUserEntity user = new CloudLoginUserEntity();
		user.setCloudScopeId(request.getCloudScopeId());
		user.setLoginUserId(request.getLoginUserId());
		user.setName(request.getUserName());
		user.setDescription(request.getDescription());
		user.setCloudUserType(type);
		CloudScopeEntity scope = Session.current().getEntityManager().find(CloudScopeEntity.class, request.getCloudScopeId(), ObjectPrivilegeMode.READ);
		user.setCloudScope(scope);
		user.setPriority(
			PersistenceUtil.findByFilter(Session.current().getEntityManager(), CloudLoginUserEntity.class, new Filter("cloudScopeId", request.getCloudScopeId())).size()
			);
		return user;
	}
	
	private void decoraiteUserLogin(final CloudLoginUserEntity user, final AddCloudLoginUserRequest request) throws CloudManagerException {
		CredentialBaseEntity entity = request.getCredential().transform(new CredentialTransformer(user.getCloudScopeId(), user.getLoginUserId()));
		user.setCredential(entity);
		try {
			HinemosEntityManager em = Session.current().getEntityManager();
			PersistenceUtil.persist(em, entity);
			em.flush();
		} catch (EntityExistsException e) {
			throw ErrorCode.LOGINUSER_ALREADY_EXIST.cloudManagerFault(user.getLoginUserId());
		}
		
		CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(user.getCloudScopeId());
		for (RoleRelation relation: request.getRoleRelations()) {
			user.getRoleRelations().add(
					new RoleRelationEntity(
						request.getCloudScopeId(),
						request.getLoginUserId(),
						relation.getRoleId()
					));
			if (!user.getCloudScope().getOwnerRoleId().equals(relation.getRoleId())) {
				try {
					CloudUtil.addFullRightToObject(AccessControllerBeanWrapper.bean(), relation.getRoleId(), HinemosModuleConstant.PLATFORM_REPOSITORY, FacilityIdUtil.getCloudScopeScopeId(scope.getPlatformId(), user.getCloudScopeId()));
				} catch (PrivilegeDuplicate | UsedObjectPrivilege | HinemosUnknown | InvalidSetting | InvalidRole | JobMasterNotFound e) {
					throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
				}
			}
		}
	}
	
	@Override
	public CloudLoginUserEntity modifyCloudLoginUser(final ModifyCloudLoginUserRequest request) throws CloudManagerException {
		final HinemosEntityManager em = Session.current().getEntityManager();
		final CloudLoginUserEntity user = em.find(CloudLoginUserEntity.class, new CloudLoginUserEntity.CloudLoginUserPK(request.getCloudScopeId(), request.getLoginUserId()), ObjectPrivilegeMode.READ);
		if (user == null) {
			throw ErrorCode.LOGINUSER_NOT_FOUND.cloudManagerFault(request.getCloudScopeId(), request.getLoginUserId());
		}
		
		if (request.getUserName() != null) {
			user.setName(request.getUserName());
		}
		if (request.getDescription() != null) {
			user.setDescription(request.getDescription());
		}
		if (request.getCredential() != null) {
			CloudManager.singleton().optionExecute(user.getCloudScope().getPlatformId(), new OptionExecutor() {
				@Override
				public void execute(ICloudOption option) throws CloudManagerException {
					CredentialBaseEntity entity = request.getCredential().transform(new CredentialTransformer(user.getCloudScopeId(), user.getLoginUserId()));
					Class<? extends CredentialBaseEntity> credentialType = option.getCloudSpec().getSupportedCredential();
	
					if (!credentialType.isAssignableFrom(entity.getClass())) {
						// TODO
						throw new InternalManagerError();
					}
					CredentialBaseEntity removed = user.getCredential();
					user.setCredential(null);
					em.remove(removed);
					em.flush();
					user.setCredential(entity);
				}
			});
		}
		return user;
	}
	
	@Override
	public CloudLoginUserEntity modifyRoleRelation(String cloudScopeId, String loginUserId, List<RoleRelation> relations) throws CloudManagerException {
		final HinemosEntityManager em = Session.current().getEntityManager();
		final CloudLoginUserEntity user = em.find(CloudLoginUserEntity.class, new CloudLoginUserEntity.CloudLoginUserPK(cloudScopeId, loginUserId), ObjectPrivilegeMode.READ);
		if (user == null) {
			throw ErrorCode.LOGINUSER_NOT_FOUND.cloudManagerFault(cloudScopeId, loginUserId);
		}
		
		CollectionComparator.compare(user.getRoleRelations(), relations, new CollectionComparator.Comparator<RoleRelationEntity, RoleRelation>() {
			@Override
			public boolean match(RoleRelationEntity o1, RoleRelation o2) {
				return o1.getRoleId().equals(o2.getRoleId());
			}
			@Override
			public void afterO1(RoleRelationEntity o1) throws CloudManagerException {
				user.getRoleRelations().remove(o1);
				em.remove(o1);

				try {
					CloudUtil.removeFullRightFromObject(AccessControllerBeanWrapper.bean(), o1.getRoleId(), HinemosModuleConstant.PLATFORM_REPOSITORY, FacilityIdUtil.getCloudScopeScopeId(user.getCloudScope().getPlatformId(), user.getCloudScopeId()));
				} catch (PrivilegeDuplicate | UsedObjectPrivilege | HinemosUnknown | InvalidSetting | InvalidRole | JobMasterNotFound e) {
					throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
				}
			}
			@Override
			public void afterO2(RoleRelation o2) throws CloudManagerException {
				user.getRoleRelations().add(
					new RoleRelationEntity(
						user.getCloudScopeId(),
						user.getLoginUserId(),
						o2.getRoleId()
					));
				try {
					CloudUtil.addFullRightToObject(AccessControllerBeanWrapper.bean(), o2.getRoleId(), HinemosModuleConstant.PLATFORM_REPOSITORY, FacilityIdUtil.getCloudScopeScopeId(user.getCloudScope().getPlatformId(), user.getCloudScopeId()));
				} catch (PrivilegeDuplicate | UsedObjectPrivilege | HinemosUnknown | InvalidSetting | InvalidRole | JobMasterNotFound e) {
					throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
				}
			}
			@Override
			public void matched(RoleRelationEntity o1, RoleRelation o2) throws CloudManagerException {
			}
		});
		return user;
	}
	
	@Override
	public void removeCloudLoginUser(String cloudScopeId, String loginUserId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		CloudLoginUserEntity user = em.find(CloudLoginUserEntity.class, new CloudLoginUserEntity.CloudLoginUserPK(cloudScopeId, loginUserId), ObjectPrivilegeMode.READ);

		
		if (user == null)
			throw ErrorCode.LOGINUSER_NOT_FOUND.cloudManagerFault(cloudScopeId, loginUserId);
		
		if (user.getCloudUserType() == CloudLoginUserEntity.CloudUserType.account)
			user.getCloudScope().setAccountId(null);

		try (RemovedEventNotifier<CloudLoginUserEntity> notifier = new RemovedEventNotifier<CloudLoginUserEntity>(CloudLoginUserEntity.class, Event_LoginUser, user)) {
			for (RoleRelationEntity relation: user.getRoleRelations()) {
				try {
					CloudUtil.removeFullRightFromObject(AccessControllerBeanWrapper.bean(), relation.getRoleId(), HinemosModuleConstant.PLATFORM_REPOSITORY, FacilityIdUtil.getCloudScopeScopeId(user.getCloudScope().getPlatformId(), user.getCloudScopeId()));
				} catch (PrivilegeDuplicate | UsedObjectPrivilege | HinemosUnknown | InvalidSetting | InvalidRole | JobMasterNotFound e) {
					Logger.getLogger(this.getClass()).error(ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e).getMessage());
				}
			}

			em.remove(user);

			notifier.completed();
		}
	}

	@Override
	public CloudLoginUserEntity getCloudLoginUser(String cloudScopeId, String loginUserId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		CloudLoginUserEntity cue = em.find(CloudLoginUserEntity.class, new CloudLoginUserEntity.CloudLoginUserPK(cloudScopeId, loginUserId), ObjectPrivilegeMode.READ);
		if (cue == null)
			throw ErrorCode.LOGINUSER_NOT_FOUND.cloudManagerFault(cloudScopeId, loginUserId);
		return cue;
	}

	@Override
	public List<CloudLoginUserEntity> getCloudLoginUserByRole(String roleId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		
		TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("findCloudLoginUser_role", CloudLoginUserEntity.class);
		query.setParameter("roleId", roleId);
		
		return query.getResultList();
	}

	@Override
	public List<CloudLoginUserEntity> getAllCloudLoginUsers() throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		return PersistenceUtil.findAll(em, CloudLoginUserEntity.class);
	}

	@Override
	public List<CloudLoginUserEntity> getCloudLoginUserByCloudScope(String cloudScopeId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		return PersistenceUtil.findByFilter(em, CloudLoginUserEntity.class, new Filter("cloudScopeId", cloudScopeId));
	}

	@Override
	public List<CloudLoginUserEntity> getCloudLoginUserByCloudScopeAndHinemosUser(String cloudScopeId, String hinemosUserId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("findCloudLoginUsers_hinemosUser_scope", CloudLoginUserEntity.class);
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("userId", hinemosUserId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
		query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
		return query.getResultList();
	}

	@Override
	public CloudLoginUserEntity getPrimaryCloudLoginUser(String cloudScopeId, String hinemosUserId) throws CloudManagerException {
		try {
			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("findPrimaryCloudLoginUser", CloudLoginUserEntity.class);
			query.setParameter("cloudScopeId", cloudScopeId);
			query.setParameter("userId", hinemosUserId);
			query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
			query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
			
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw ErrorCode.LOGINUSER_NOT_FOUND.cloudManagerFault(cloudScopeId, hinemosUserId);
		}
	}

	@Override
	public CloudLoginUserEntity getPrimaryCloudLoginUserByCurrent(String cloudScopeId) throws CloudManagerException {
		return getPrimaryCloudLoginUser(cloudScopeId, Session.current().getHinemosCredential().getUserId());
	}

	@Override
	public List<String> getCloudLoginUserPriority(String cloudScopeId) {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("getCloudLoginUserPriority", CloudLoginUserEntity.class);
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
		
		List<String> users = new ArrayList<>();
		for (CloudLoginUserEntity user: query.getResultList()) {
			users.add(user.getLoginUserId());
		}
		return users;
	}

	@Override
	public void modifyCloudLoginUserPriority(String cloudScopeId, List<String> cloudLoginUserIds) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("getCloudLoginUserPriority", CloudLoginUserEntity.class);
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
		
		List<CloudLoginUserEntity> result = new ArrayList<>(query.getResultList());
		if (result.size() != cloudLoginUserIds.size()) {
			throw ErrorCode.LOGINUSER_NUM_NOT_MATCH.cloudManagerFault(cloudLoginUserIds.toString());
		}
		
		for (int i = 0; i < cloudLoginUserIds.size(); ++i) {
			Iterator<CloudLoginUserEntity> iter = result.iterator();
			int before = result.size();
			while (iter.hasNext()) {
				CloudLoginUserEntity user = iter.next();
				if (user.getLoginUserId().equals(cloudLoginUserIds.get(i))) {
					user.setPriority(i + 1);
					iter.remove();
					break;
				}
			}
			if (before == result.size()) {
				throw ErrorCode.LOGINUSER_USER_NOT_INCLUDE.cloudManagerFault(cloudLoginUserIds.toString());
			}
		}
		if (!result.isEmpty()) {
			throw ErrorCode.LOGINUSER_USER_NOT_INCLUDE.cloudManagerFault(cloudLoginUserIds.toString());
		}
	}
}
