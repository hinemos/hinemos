/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ObjectPrivilegeInfoRequestP1 implements RequestDto {

	public ObjectPrivilegeInfoRequestP1() {
	}

	@RestItemName(value = MessageConstant.ROLE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String roleId;

	@RestItemName(value = MessageConstant.OBJECT_PRIVILEGE)
	@RestValidateString(notNull = true, maxLen = 64)
	private String objectPrivilege;

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

	@Override
	public String toString() {
		return "ObjectPrivilegeInfoP1 [roleId=" + roleId + ", objectPrivilege=" + objectPrivilege + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
