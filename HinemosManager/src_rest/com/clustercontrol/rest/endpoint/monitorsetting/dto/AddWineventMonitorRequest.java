/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddWineventMonitorRequest extends AbstractAddStringMonitorRequest {

	public AddWineventMonitorRequest() {

	}

	private WinEventCheckInfoRequest winEventCheckInfo;

	public WinEventCheckInfoRequest getWinEventCheckInfo() {
		return winEventCheckInfo;
	}

	public void setWinEventCheckInfo(WinEventCheckInfoRequest winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}

	@Override
	public String toString() {
		return "AddWineventMonitorRequest [winEventCheckInfo=" + winEventCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
