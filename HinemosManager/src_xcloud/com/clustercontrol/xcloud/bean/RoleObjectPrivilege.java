/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.Set;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;

public class RoleObjectPrivilege {
	private String roleId;
	private Set<ObjectPrivilegeMode> privilegeModes;

	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public Set<ObjectPrivilegeMode> getPrivilegeModes() {
		return privilegeModes;
	}
	public void setPrivilegeModes(Set<ObjectPrivilegeMode> privilegeModes) {
		this.privilegeModes = privilegeModes;
	}
}
