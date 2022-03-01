/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.List;

public class RoleInfoResponseP3 {

	public RoleInfoResponseP3() {
	}

	private List<UserInfoResponseP4> userInfoList;

	public List<UserInfoResponseP4> getUserInfoList() {
		return userInfoList;
	}

	public void setUserInfoList(List<UserInfoResponseP4> userInfoList) {
		this.userInfoList = userInfoList;
	}

	@Override
	public String toString() {
		return "RoleInfoResponseP3 [userInfoList=" + userInfoList + "]";
	}

}
