/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddWinserviceMonitorRequest extends AbstractAddTruthMonitorRequest {

	public AddWinserviceMonitorRequest() {

	}

	private WinServiceCheckInfoRequest winServiceCheckInfo;

	public WinServiceCheckInfoRequest getWinServiceCheckInfo() {
		return winServiceCheckInfo;
	}

	public void setWinServiceCheckInfo(WinServiceCheckInfoRequest winServiceCheckInfo) {
		this.winServiceCheckInfo = winServiceCheckInfo;
	}

	@Override
	public String toString() {
		return "AddWinserviceMonitorRequest [winServiceCheckInfo=" + winServiceCheckInfo
				+ ", truthValueInfo=" + truthValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
