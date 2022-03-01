/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = ObjectPrivilegeInfo.class, idName = "id")
public class ObjectPrivilegeInfoResponseP1 {

	public ObjectPrivilegeInfoResponseP1() {
	}

	private String roleId;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	@Override
	public String toString() {
		return "ObjectPrivilegeInfoResponseP1 [roleId=" + roleId + "]";
	}

}
