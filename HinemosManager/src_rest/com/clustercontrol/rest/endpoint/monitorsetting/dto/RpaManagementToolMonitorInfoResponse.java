/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class RpaManagementToolMonitorInfoResponse extends AbstractTruthMonitorResponse {
	public RpaManagementToolMonitorInfoResponse() {
	}
	private RpaManagementToolServiceCheckInfoResponse rpaManagementToolServiceCheckInfo;

	public RpaManagementToolServiceCheckInfoResponse getRpaManagementToolServiceCheckInfo() {
		return rpaManagementToolServiceCheckInfo;
	}

	public void setRpaManagementToolServiceCheckInfo(RpaManagementToolServiceCheckInfoResponse rpaManagementToolServiceCheckInfo) {
		this.rpaManagementToolServiceCheckInfo = rpaManagementToolServiceCheckInfo;
	}
}
