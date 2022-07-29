/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class WinserviceMonitorInfoResponse extends AbstractTruthMonitorResponse {

	private WinServiceCheckInfoResponse winServiceCheckInfo;

	public WinserviceMonitorInfoResponse() {
	}

	public WinServiceCheckInfoResponse getWinServiceCheckInfo() {
		return winServiceCheckInfo;
	}

	public void setWinServiceCheckInfo(WinServiceCheckInfoResponse winServiceCheckInfo) {
		this.winServiceCheckInfo = winServiceCheckInfo;
	}

	@Override
	public String toString() {
		return "WinserviceMonitorInfoResponse [winServiceCheckInfo=" + winServiceCheckInfo + ", truthValueInfo="
				+ truthValueInfo + ", monitorId=" + monitorId + ", application=" + application + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", facilityId="
				+ facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}