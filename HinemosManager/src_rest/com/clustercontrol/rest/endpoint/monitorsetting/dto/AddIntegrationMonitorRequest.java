/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddIntegrationMonitorRequest extends AbstractAddTruthMonitorRequest {

	public AddIntegrationMonitorRequest() {

	}

	private IntegrationCheckInfoRequest integrationCheckInfo;

	public IntegrationCheckInfoRequest getIntegrationCheckInfo() {
		return integrationCheckInfo;
	}

	public void setIntegrationCheckInfo(IntegrationCheckInfoRequest integrationCheckInfo) {
		this.integrationCheckInfo = integrationCheckInfo;
	}

	@Override
	public String toString() {
		return "AddIntegrationMonitorRequest [integrationCheckInfo=" + integrationCheckInfo + ", truthValueInfo="
				+ truthValueInfo + ", monitorId=" + monitorId + ", application=" + application + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", calendarId="
				+ calendarId + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList
				+ ", ownerRoleId=" + ownerRoleId + "]";
	}

}
