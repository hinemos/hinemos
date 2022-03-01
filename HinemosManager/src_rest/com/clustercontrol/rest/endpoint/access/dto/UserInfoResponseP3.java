/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.List;

public class UserInfoResponseP3 {

	public UserInfoResponseP3() {
	}

	private List<RoleInfoResponseP1> roleList;

	public List<RoleInfoResponseP1> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<RoleInfoResponseP1> roleList) {
		this.roleList = roleList;
	}

	@Override
	public String toString() {
		return "UserInfoResponseP3 [roleList=" + roleList + "]";
	}

}
