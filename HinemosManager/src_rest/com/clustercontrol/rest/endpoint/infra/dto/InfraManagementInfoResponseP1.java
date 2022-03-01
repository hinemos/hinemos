/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

public class InfraManagementInfoResponseP1 {
	
	private String managementId;

	public InfraManagementInfoResponseP1() {
	}

	public InfraManagementInfoResponseP1(String managementId) {
		this.setManagementId(managementId);
	}

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}

	@Override
	public String toString() {
		return "InfraManagementInfoResponseP1 [managementId=" + managementId + "]";
	}

}
