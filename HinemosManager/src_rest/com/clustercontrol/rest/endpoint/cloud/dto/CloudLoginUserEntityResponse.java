/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity.CloudUserType;

public class CloudLoginUserEntityResponse {
	private String cloudScopeId;
	private String loginUserId;
	private String name;
	private String description;
	private CloudUserType cloudUserType;
	private List<RoleRelationResponse> roleRelations;
	private Integer priority;
	@RestBeanConvertDatetime
	private String regDate;
	@RestBeanConvertDatetime
	private String updateDate;
	private String regUser;
	private String updateUser;
	private CredentialResponse credential;

	public CloudLoginUserEntityResponse() {
	}

	public String getCloudScopeId() {
		return cloudScopeId;
	}

	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	public String getLoginUserId() {
		return loginUserId;
	}

	public void setLoginUserId(String loginUserId) {
		this.loginUserId = loginUserId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public com.clustercontrol.xcloud.bean.CloudLoginUser.CloudUserType getCloudUserType() {
		return com.clustercontrol.xcloud.bean.CloudLoginUser.CloudUserType.valueOf(cloudUserType.name());
	}

	public void setCloudUserType(CloudUserType cloudUserType) {
		this.cloudUserType = cloudUserType;
	}

	public List<RoleRelationResponse> getRoleRelations() {
		return roleRelations;
	}

	public void setRoleRelations(List<RoleRelationResponse> roleRelations) {
		this.roleRelations = roleRelations;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public CredentialResponse getCredential() {
		return credential;
	}

	public void setCredential(CredentialResponse credential) {
		this.credential = credential;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
}
