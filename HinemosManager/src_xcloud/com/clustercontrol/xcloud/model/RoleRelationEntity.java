/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.model.RoleInfo;

@Entity
@Table(name = "cc_cfg_xcloud_role_relation", schema="setting")
@IdClass(RoleRelationEntity.RoleRealationEntityPK.class)
public class RoleRelationEntity extends EntityBase implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class RoleRealationEntityPK implements java.io.Serializable {
		private static final long serialVersionUID = 1L;

		private String cloudScopeId;
		private String roleId;

		public RoleRealationEntityPK() {
		}

		public RoleRealationEntityPK(String cloudScopeId, String roleId) {
			this.cloudScopeId = cloudScopeId;
			this.roleId = roleId;
		}


		public String getCloudScopeId() {
			return cloudScopeId;
		}
		public void setCloudScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}

		public String getRoleId() {
			return roleId;
		}
		public void setRoleId(String roleId) {
			this.roleId = roleId;
		}
	}

	private String cloudScopeId;
	private String loginUserId;
	private String roleId;

	private CloudLoginUserEntity loginUser;
	private RoleInfo role;

	public RoleRelationEntity() {
	}

	public RoleRelationEntity(String cloudScopeId, String loginUserId, String roleId) {
		this.cloudScopeId = cloudScopeId;
		this.loginUserId = loginUserId;
		this.roleId = roleId;
	}

	@Id
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@Column(name="cloud_login_user_id")
	public String getLoginUserId() {
		return loginUserId;
	}
	public void setLoginUserId(String loginUserId) {
		this.loginUserId = loginUserId;
	}

	@Id
	@Column(name="role_id")
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="cloud_login_user_id", referencedColumnName="cloud_login_user_id", insertable=false, updatable=false),
	})
	public CloudLoginUserEntity getLoginUser() {
		return this.loginUser;
	}
	public void setLoginUser(CloudLoginUserEntity loginUser) {
		this.loginUser = loginUser;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="role_id", referencedColumnName="role_id", insertable=false, updatable=false)
	public RoleInfo getRole() {
		return this.role;
	}
	public void setRole(RoleInfo role) {
		this.role = role;
	}

	@Override
	public RoleRealationEntityPK getId() {
		return new RoleRealationEntityPK(getCloudScopeId(), getRoleId());
	}
}
