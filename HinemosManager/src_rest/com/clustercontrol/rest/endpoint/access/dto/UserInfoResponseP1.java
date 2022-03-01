/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.rest.annotation.cmdtool.PasswordTypeParam;

public class UserInfoResponseP1 {
	
	public UserInfoResponseP1() {
	}
	
	private String userId;
	@PasswordTypeParam
	private String password;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "UserInfoResponseP1 [userId=" + userId + ", password=" + password + "]";
	}

}
