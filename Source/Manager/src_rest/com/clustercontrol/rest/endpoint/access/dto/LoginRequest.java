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
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class LoginRequest implements RequestDto {

	public LoginRequest() {
	}

	@RestItemName(value = MessageConstant.USER_ID)
	private String userId;

	@RestItemName(value = MessageConstant.PASSWORD)
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
		return "LoginRequest [userId=" + userId + ", password=" + password + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
