/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class WineventMonitorInfoResponse extends AbstractStringMonitorResponse {

	private WinEventCheckInfoResponse winEventCheckInfo;

	public WineventMonitorInfoResponse() {
	}

	public WinEventCheckInfoResponse getWinEventCheckInfo() {
		return winEventCheckInfo;
	}

	public void setWinEventCheckInfo(WinEventCheckInfoResponse winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}

	@Override
	public String toString() {
		return "WineventMonitorInfoResponse [winEventCheckInfo=" + winEventCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

}