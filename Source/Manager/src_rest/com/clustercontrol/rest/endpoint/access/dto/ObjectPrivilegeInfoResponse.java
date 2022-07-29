/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;

@RestBeanConvertIdClassSet(infoClass=ObjectPrivilegeInfo.class,idName="id")
public class ObjectPrivilegeInfoResponse {
	
	public ObjectPrivilegeInfoResponse() {
	}

	private String objectType;
	private String objectId;
	private String roleId;
	private String objectPrivilege;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String createDate;
	private String createUserId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String modifyDate;
	private String modifyUserId;

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getObjectPrivilege() {
		return objectPrivilege;
	}

	public void setObjectPrivilege(String objectPrivilege) {
		this.objectPrivilege = objectPrivilege;
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

	@Override
	public String toString() {
		return "ObjectPrivilegeInfoResponse [objectType=" + objectType + ", objectId=" + objectId + ", roleId=" + roleId
				+ ", objectPrivilege=" + objectPrivilege + ", createDate=" + createDate + ", createUserId="
				+ createUserId + ", modifyDate=" + modifyDate + ", modifyUserId=" + modifyUserId + "]";
	}

}
