/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class IntegrationMonitorInfoResponse extends AbstractTruthMonitorResponse {

	private IntegrationCheckInfoResponse integrationCheckInfo;

	public IntegrationMonitorInfoResponse() {
	}

	public IntegrationCheckInfoResponse getIntegrationCheckInfo() {
		return integrationCheckInfo;
	}

	public void setIntegrationCheckInfo(IntegrationCheckInfoResponse integrationCheckInfo) {
		this.integrationCheckInfo = integrationCheckInfo;
	}

	@Override
	public String toString() {
		return "IntegrationMonitorInfoResponse [integrationCheckInfo=" + integrationCheckInfo + ", truthValueInfo="
				+ truthValueInfo + ", monitorId=" + monitorId + ", application=" + application + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", facilityId="
				+ facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

	
}