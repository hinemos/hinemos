/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.Table;


/**
 * インスタンス情報のBMP Entity Bean クラス<BR>
 */
@NamedQueries({
	@NamedQuery(
			name="findCloudLoginUser_role",
			query="SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr WHERE rr.roleId = :roleId"
			),
	@NamedQuery(
			name="findCloudLoginUsers_hinemosUser_scope",
			query="SELECT DISTINCT u FROM CloudLoginUserEntity u "
					+ "WHERE u.cloudScopeId = :cloudScopeId "
					+ "AND ("
					+ "EXISTS (SELECT r FROM UserInfo h JOIN h.roleList r WHERE h.userId = :userId AND r.roleId = :ADMINISTRATORS) "
					+ "OR "
					+ "EXISTS (SELECT h FROM u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId LEFT JOIN r.userInfoList h WHERE h.userId = :userId) "
					+ "OR "
					+ "EXISTS (SELECT u2 FROM CloudLoginUserEntity u2 JOIN u2.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u2.cloudScopeId = :cloudScopeId AND h.userId = :userId AND u2.cloudUserType = :accountType)"
					+ ")"
			),
	@NamedQuery(
			name="findCloudLoginUser_hinemosUser_scope",
			query="SELECT DISTINCT u FROM CloudLoginUserEntity u "
					+ "WHERE u.cloudScopeId = :cloudScopeId AND u.loginUserId = :loginUserId "
					+ "AND ("
					+ "EXISTS (SELECT r FROM UserInfo h JOIN h.roleList r WHERE h.userId = :userId AND r.roleId = :ADMINISTRATORS) "
					+ "OR "
					+ "EXISTS (SELECT h FROM u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId LEFT JOIN r.userInfoList h WHERE h.userId = :userId) "
					+ "OR "
					+ "EXISTS (SELECT u2 FROM CloudLoginUserEntity u2 JOIN u2.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u2.cloudScopeId = :cloudScopeId AND h.userId = :userId AND u2.cloudUserType = :accountType)"
					+ ")"
			),
	@NamedQuery(
			name="findPrimaryCloudLoginUser",
			query="SELECT u FROM CloudLoginUserEntity u WHERE u.cloudScopeId = :cloudScopeId AND u.priority = "
					+ "(SELECT MIN(u.priority) FROM CloudLoginUserEntity u "
					+ "WHERE u.cloudScopeId = :cloudScopeId "
					+ "AND ("
					+ "EXISTS (SELECT r FROM UserInfo h JOIN h.roleList r WHERE h.userId = :userId AND r.roleId = :ADMINISTRATORS) "
					+ "OR "
					+ "EXISTS (SELECT h FROM u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId LEFT JOIN r.userInfoList h WHERE h.userId = :userId) "
					+ "OR "
					+ "EXISTS (SELECT u2 FROM CloudLoginUserEntity u2 JOIN u2.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u2.cloudScopeId = :cloudScopeId AND h.userId = :userId AND u2.cloudUserType = :accountType)"
					+ ")) "
			),
	@NamedQuery(
			name="getCloudLoginUserPriority",
			query="SELECT u FROM CloudLoginUserEntity u WHERE u.cloudScopeId = :cloudScopeId AND u.cloudUserType <> :accountType ORDER BY u.priority ASC"
			),
	@NamedQuery(
			name="findCloudLoginUser_account",
			query="SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u.cloudScopeId = :cloudScopeId AND h.userId = :userId AND u.cloudUserType = :accountType"
			),
	@NamedQuery(
			name="findCloudLoginUser_account_self",
			query="SELECT u FROM CloudLoginUserEntity u JOIN u.roleRelations rr JOIN RoleInfo r ON rr.roleId = r.roleId JOIN r.userInfoList h WHERE u.cloudScopeId = :cloudScopeId AND h.userId = :userId AND (u.cloudUserType = :accountType OR u.loginUserId = :loginUserId)"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_login_user", schema="setting")
@IdClass(CloudLoginUserEntity.CloudLoginUserPK.class)
public class CloudLoginUserEntity extends HinemosObjectEntity implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class CloudLoginUserPK implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		
		private String cloudScopeId;
		private String loginUserId;

		public CloudLoginUserPK() {
		}

		public CloudLoginUserPK(String cloudScopeId, String loginUserId) {
			this.cloudScopeId = cloudScopeId;
			this.loginUserId = loginUserId;
		}

		public String getCloudScopeId() {
			return cloudScopeId;
		}
		public void setCloudScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}

		public String getLoginUserId() {
			return this.loginUserId;
		}
		public void setLoginUserId(String loginUserId) {
			this.loginUserId = loginUserId;
		}
	}
	
	public static enum CloudUserType {
		account,
		user
	}
	
	private String cloudScopeId;
	private String loginUserId;
	private String name;
	private String description;
	private CloudUserType cloudUserType;
	private List<RoleRelationEntity> roleRelations;
	
	private int priority;

	private CloudScopeEntity cloudScope;

	private CredentialBaseEntity credential;

	public CloudLoginUserEntity()
	{
	}

	public CloudLoginUserEntity(String cloudScopeId, String loginUserId, String name, String description, CredentialBaseEntity credential, List<RoleRelationEntity> roleRelations, CloudUserType cloudUserType, Long regDate, Long updateDate, String regUser, String updateUser) {
		super(regDate, updateDate, regUser, updateUser);
		setCloudScopeId(cloudScopeId);
		setLoginUserId(loginUserId);
		setName(name);
		setDescription(description);
		setCredential(credential);
		setRoleRelations(roleRelations);
		setCloudUserType(cloudUserType);
	}
	
	@Id
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return this.cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	
	@Id
	@Column(name="cloud_login_user_id")
	public String getLoginUserId() {
		return this.loginUserId;
	}
	public void setLoginUserId(String loginUserId) {
		this.loginUserId = loginUserId;
	}
	
	@Column(name="user_name")
	public String getName()	{
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}	

	@Column(name="description")
	public String getDescription()
	{
		return this.description;
	}
	public void setDescription( String description )
	{
		this.description = description;
	}

	@Column(name="cloud_user_type")
	@Enumerated(EnumType.STRING)
	public CloudUserType getCloudUserType()	{
		return this.cloudUserType;
	}
	public void setCloudUserType(CloudUserType cloudUserType)	{
		this.cloudUserType = cloudUserType;
	}

	@Column(name="priority")
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false)
	public CloudScopeEntity getCloudScope() {
		return cloudScope;
	}
	public void setCloudScope(CloudScopeEntity scope) {
		this.cloudScope = scope;
	}

	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumns({
		@PrimaryKeyJoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id"),
		@PrimaryKeyJoinColumn(name="cloud_login_user_id", referencedColumnName="cloud_login_user_id")
	})
	public CredentialBaseEntity getCredential() {
		return credential;
	}
	public void setCredential(CredentialBaseEntity credential) {
		this.credential = credential;
	}

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="loginUser")
	public List<RoleRelationEntity> getRoleRelations() {
		return roleRelations;
	}
	public void setRoleRelations(List<RoleRelationEntity> roleRelations) {
		this.roleRelations = roleRelations;
	}
	
	@Override
	public CloudLoginUserPK getId() {
		return new CloudLoginUserPK(getCloudScopeId(), getLoginUserId());
	}
}
