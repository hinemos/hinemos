/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.RoleTypeEnum;

public class RoleInfoResponse {
	
	public RoleInfoResponse() {
	}
	
	private String roleId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String createDate;
	private String createUserId;
	private String description;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String modifyDate;
	private String modifyUserId;
	@RestPartiallyTransrateTarget
	private String roleName;
	@RestBeanConvertEnum
	private RoleTypeEnum roleType;
	private List<UserInfoResponse> userInfoList;
	private List<SystemPrivilegeInfoResponse> systemPrivilegeList;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public RoleTypeEnum getRoleType() {
		return roleType;
	}

	public void setRoleType(RoleTypeEnum roleType) {
		this.roleType = roleType;
	}

	public List<UserInfoResponse> getUserInfoList() {
		return userInfoList;
	}

	public void setUserInfoList(List<UserInfoResponse> userInfoList) {
		this.userInfoList = userInfoList;
	}

	public List<SystemPrivilegeInfoResponse> getSystemPrivilegeList() {
		return systemPrivilegeList;
	}

	public void setSystemPrivilegeList(List<SystemPrivilegeInfoResponse> systemPrivilegeList) {
		this.systemPrivilegeList = systemPrivilegeList;
	}

	@Override
	public String toString() {
		return "RoleInfoResponse [roleId=" + roleId + ", createDate=" + createDate + ", createUserId=" + createUserId
				+ ", description=" + description + ", modifyDate=" + modifyDate + ", modifyUserId=" + modifyUserId
				+ ", roleName=" + roleName + ", roleType=" + roleType + ", userInfoList=" + userInfoList
				+ ", systemPrivilegeList=" + systemPrivilegeList + "]";
	}

}
