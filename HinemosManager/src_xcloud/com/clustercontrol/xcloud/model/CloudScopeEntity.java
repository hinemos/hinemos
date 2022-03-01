/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.MapKey;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.IPrivateCloudOption;
import com.clustercontrol.xcloud.factory.IPublicCloudOption;

@Entity
@NamedQueries({
	@NamedQuery(
			name="findCloudScopeByHinemosUser",
			query="SELECT DISTINCT s FROM CloudScopeEntity s "
					+ "WHERE s.cloudScopeId = :cloudScopeId "
					+ "AND ("
					+ "EXISTS (SELECT u FROM UserInfo u JOIN u.roleList r WHERE u.userId = :userId AND r.roleId = :ADMINISTRATORS) "
					+ "OR "
					+ "EXISTS (SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u.cloudScopeId = :cloudScopeId AND h.userId = :userId)"
					+ ")"
			),
	@NamedQuery(
			name="findCloudScopesByHinemosUser",
			query="SELECT DISTINCT s FROM CloudScopeEntity s "
					+ "WHERE "
					+ "EXISTS (SELECT u FROM UserInfo u JOIN u.roleList r WHERE u.userId = :userId AND r.roleId = :ADMINISTRATORS) "
					+ "OR "
					+ "EXISTS (SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u.cloudScopeId = s.cloudScopeId AND h.userId = :userId)"
			),
	@NamedQuery(
			name="findCloudScopeByHinemosUserAsAdmin",
			query="SELECT DISTINCT s FROM CloudScopeEntity s "
					+ "WHERE s.cloudScopeId = :cloudScopeId "
					+ "AND ("
					+ "EXISTS (SELECT u FROM UserInfo u JOIN u.roleList r WHERE u.userId = :userId AND r.roleId = :ADMINISTRATORS) "
					+ "OR "
					+ "EXISTS (SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u.cloudScopeId = :cloudScopeId AND h.userId = :userId AND u.cloudUserType = :accountType)"
					+ ")"
			),
	@NamedQuery(
			name="findCloudScopeByOwnerRole",
			query="SELECT DISTINCT s FROM CloudScopeEntity s "
					+ "WHERE s.cloudScopeId = :cloudScopeId "
					+ "AND ("
					+ ":roleId = :ADMINISTRATORS "
					+ "OR "
					+ "EXISTS (SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr WHERE u.cloudScopeId = s.cloudScopeId AND rr.roleId = :roleId)"
					+ ")"
			),
	@NamedQuery(
			name="findCloudScopesByOwnerRole",
			query="SELECT DISTINCT s FROM CloudScopeEntity s "
					+ "WHERE "
					+ ":roleId = :ADMINISTRATORS "
					+ "OR "
					+ "EXISTS (SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr WHERE u.cloudScopeId = s.cloudScopeId AND rr.roleId = :roleId)"
			),
	@NamedQuery(
			name="findChildFacilityIdList",
			query="SELECT r.childFacilityId FROM FacilityRelationEntity r WHERE r.parentFacilityId = :facilityId"
			),
	@NamedQuery(
			name="updateAccountOnCloudScope",
			query="UPDATE CloudScopeEntity s SET s.account = :account WHERE s.cloudScopeId = :cloudScopeId"
			),
	@NamedQuery(
			name=CloudScopeEntity.updatebillingLastDate,
			query="UPDATE CloudScopeEntity AS s SET s.billingLastDate = :billingLastDate WHERE s.cloudScopeId = :cloudScopeId AND (s.billingLastDate < :billingLastDate OR s.billingLastDate IS NULL)"
			),
	@NamedQuery(
			name=CloudScopeEntity.findAvailableRoles,
			query="SELECT r FROM RoleInfo AS r, UserInfo AS h WHERE r MEMBER OF h.roleList AND h.userId = :userId AND NOT r.roleId IN :excludes"
			),
	@NamedQuery(
			name=CloudScopeEntity.findAvailableRolesAsAdmin,
			query="SELECT r FROM RoleInfo AS r WHERE NOT r.roleId IN :excludes"
			),
})
@Table(name = "cc_cfg_xcloud_scope", schema="setting")
@DiscriminatorColumn(name="cloud_type")
@Inheritance
public abstract class CloudScopeEntity extends HinemosObjectEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String findAvailableRoles = "findAvailableRoles";
	public static final String findAvailableRolesAsAdmin = "findAvailableRolesAsAdmin";
	public static final String updatebillingLastDate = "updatebillingLastDate";
	
	@FunctionalInterface
	public interface OptionExecutor {
		void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException;
	}
	
	@FunctionalInterface
	public interface OptionCallable<T> {
		T call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException;
	}

	public interface OptionExecutorEx {
		void execute(PrivateCloudScopeEntity scope, IPrivateCloudOption option) throws CloudManagerException;
		void execute(PublicCloudScopeEntity scope, IPublicCloudOption option) throws CloudManagerException;
	}

	public interface OptionCallableEx<T> {
		T call(PrivateCloudScopeEntity scope, IPrivateCloudOption option) throws CloudManagerException;
		T call(PublicCloudScopeEntity scope, IPublicCloudOption option) throws CloudManagerException;
	}
	
	public interface IVisitor {
		void visit(PublicCloudScopeEntity scope) throws CloudManagerException;
		void visit(PrivateCloudScopeEntity scope) throws CloudManagerException;
	}
	
	public interface ITransformer<T> {
		T transform(PublicCloudScopeEntity scope) throws CloudManagerException;
		T transform(PrivateCloudScopeEntity scope) throws CloudManagerException;
	}
	
	public static class Visitor implements IVisitor {
		@Override
		public void visit(PublicCloudScopeEntity scope) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void visit(PrivateCloudScopeEntity scope) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
	}	
	public static class Transformer<T> implements ITransformer<T> {
		@Override
		public T transform(PublicCloudScopeEntity scope) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public T transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
	}
	
	private String platformId;
	private String description;
	private String cloudScopeId;
	private String ownerRoleId;
	private String name;
	private String accountId;
	private Boolean billingDetailCollectorFlg = false;
	private Integer retentionPeriod;
	private Long billingLastDate;
	private Map<String, ExtendedProperty> extendedProperties = new HashMap<>();
	
	private CloudPlatformEntity platform;

	private CloudLoginUserEntity account;

	public CloudScopeEntity() {
	}
	public CloudScopeEntity(String platformId, String cloudScopeId, String name, String accountId, Long regDate, Long updateDate, String regUser, String updateUser) {
		super(regDate, updateDate, regUser, updateUser);
		setPlatformId(platformId);
		setCloudScopeId(cloudScopeId);
		setName(name);
		setAccountId(accountId);
	}

	@Column(name="cloud_platform_id")
	public String getPlatformId() {
		return platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	@Id
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@Column(name="cloud_scope_name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	@Column(name="owner_role_id")
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="account_id", insertable=false, updatable=false)
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	@Column(name="billing_detail_collector_flg")
	public Boolean getBillingDetailCollectorFlg() {
		return billingDetailCollectorFlg;
	}
	
	public void setBillingDetailCollectorFlg(Boolean billingDetailCollectorFlg) {
		this.billingDetailCollectorFlg = billingDetailCollectorFlg;
	}

	@Column(name="retention_period")
	public Integer getRetentionPeriod() {
		return retentionPeriod;
	}
	public void setRetentionPeriod(Integer retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}

	@Column(name="billing_last_date")
	public Long getBillingLastDate() {
		return billingLastDate;
	}
	public void setBillingLastDate(Long billingLastDate) {
		this.billingLastDate = billingLastDate;
	}
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="cloud_platform_id", referencedColumnName="cloud_platform_id", insertable=false, updatable=false)
	public CloudPlatformEntity getPlatform() {
		return this.platform;
	}
	public void setPlatform(CloudPlatformEntity platform) {
		this.platform = platform;
	}

	@OneToOne(cascade={CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH}, fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="account_id", referencedColumnName="cloud_login_user_id", insertable=true, updatable=true)
	})
	public CloudLoginUserEntity getAccount() {
		return this.account;
	}
	public void setAccount(CloudLoginUserEntity account) {
		this.account = account;
	}

	@Override
	public String getId() {
		return getCloudScopeId();
	}
	
	public void optionExecute(final OptionExecutor executor) throws CloudManagerException {
		try {
			Session.current().set(this.getClass(), this);
			
			CloudManager.singleton().optionExecute(getPlatformId(), new CloudManager.OptionExecutor() {
				@Override
				public void execute(ICloudOption option) throws CloudManagerException {
					executor.execute(CloudScopeEntity.this, option);
				}
			});
		} finally {
			Session.current().set(this.getClass(), null);
		}
	}
	
	public <T> T optionCall(final OptionCallable<T> callable) throws CloudManagerException {
		CloudScopeEntity prev = Session.current().get(CloudScopeEntity.class);
		try {
			Session.current().set(CloudScopeEntity.class, this);
			
			return CloudManager.singleton().optionCall(getPlatformId(), new CloudManager.OptionCallable<T>() {
				@Override
				public T call(ICloudOption option) throws CloudManagerException {
					return callable.call(CloudScopeEntity.this, option);
				}
			});
		} finally {
			Session.current().set(CloudScopeEntity.class, prev);
		}
	}
	
	public void optionExecuteEx(final OptionExecutorEx executor) throws CloudManagerException {
		CloudScopeEntity prev = Session.current().get(CloudScopeEntity.class);
		try {
			Session.current().set(this.getClass(), this);
			
			CloudManager.singleton().optionExecute(getPlatformId(), new CloudManager.OptionExecutor() {
				@Override
				public void execute(ICloudOption option) throws CloudManagerException {
					option.visit(new ICloudOption.IVisitor() {
						@Override
						public void visit(final IPublicCloudOption cloudOption) throws CloudManagerException {
							CloudScopeEntity.this.visit(new CloudScopeEntity.Visitor() {
								@Override
								public void visit(PublicCloudScopeEntity scope) throws CloudManagerException {
									executor.execute(scope, cloudOption);
								}
							});
						}
						@Override
						public void visit(final IPrivateCloudOption cloudOption) throws CloudManagerException {
							CloudScopeEntity.this.visit(new CloudScopeEntity.Visitor() {
								@Override
								public void visit(PrivateCloudScopeEntity scope) throws CloudManagerException {
									executor.execute(scope, cloudOption);
								}
							});
						}
					});
				}
			});
		} finally {
			Session.current().set(this.getClass(), prev);
		}
	}
	
	public <T> T callOptionEx(final OptionCallableEx<T> callable) throws CloudManagerException {
		CloudScopeEntity prev = Session.current().get(CloudScopeEntity.class);
		try {
			Session.current().set(this.getClass(), this);
			
			return CloudManager.singleton().optionCall(getPlatformId(), new CloudManager.OptionCallable<T>() {
				@Override
				public T call(ICloudOption option) throws CloudManagerException {
					return option.transform(new ICloudOption.ITransformer<T>() {
						@Override
						public T transform(final IPrivateCloudOption cloudOption) throws CloudManagerException {
							return CloudScopeEntity.this.transform(new CloudScopeEntity.Transformer<T>() {
								@Override
								public T transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
									return callable.call(scope, cloudOption);
								}
							});
						}
						@Override
						public T transform(final IPublicCloudOption cloudOption) throws CloudManagerException {
							return CloudScopeEntity.this.transform(new CloudScopeEntity.Transformer<T>() {
								@Override
								public T transform(PublicCloudScopeEntity scope) throws CloudManagerException {
									return callable.call(scope, cloudOption);
								}
							});
						}
					});
				}
			});
		} finally {
			Session.current().set(this.getClass(), prev);
		}
	}
	
	@ElementCollection
	@CollectionTable(
		name="cc_cfg_xcloud_scope_eprop", schema="setting",
		joinColumns=@JoinColumn(name="cloud_scope_id")
	)
	@MapKey(name="name")
	@AttributeOverrides({
		@AttributeOverride(name="name", column=@Column(name="name")),
		@AttributeOverride(name="value", column=@Column(name="value"))
	})
	public Map<String, ExtendedProperty> getExtendedProperties() {
		return extendedProperties;
	}
	public void setExtendedProperties(Map<String, ExtendedProperty> extendedProperties) {
		this.extendedProperties = extendedProperties;
	}

	public abstract boolean isPublic();
	
	public abstract List<LocationEntity> getLocations() throws CloudManagerException;
	
	public abstract LocationEntity getLocation(String locationId) throws CloudManagerException;
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException;
	
	public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException;
}
