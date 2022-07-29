/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

public class LoginResponse {

	public LoginResponse() {
	}

	private HinemosToken token;
	private ManagerInfoResponse managerInfo;

	public HinemosToken getToken() {
		return token;
	}

	public void setToken(HinemosToken token) {
		this.token = token;
	}

	public ManagerInfoResponse getManagerInfo() {
		return managerInfo;
	}

	public void setManagerInfo(ManagerInfoResponse managerInfo) {
		this.managerInfo = managerInfo;
	}

	@Override
	public String toString() {
		return "LoginResponse [token=" + token + ", managerInfo=" + managerInfo + "]";
	}

}
