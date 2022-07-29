/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import java.util.List;

public class InfraSessionResponse {

	private String sessionId;
	private String managementId;
	private List<String> moduleList;

	public InfraSessionResponse() {
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}

	public List<String> getModuleList() {
		return moduleList;
	}

	public void setModuleList(List<String> moduleList) {
		this.moduleList = moduleList;
	}

	@Override
	public String toString() {
		return "InfraSessionResponse [sessionId=" + sessionId + ", managementId=" + managementId + ", moduleList="
				+ moduleList + "]";
	}

}
