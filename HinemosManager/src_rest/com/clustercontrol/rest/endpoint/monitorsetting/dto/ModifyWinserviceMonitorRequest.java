/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyWinserviceMonitorRequest extends AbstractModifyTruthMonitorRequest {

	public ModifyWinserviceMonitorRequest() {

	}

	private WinServiceCheckInfoRequest winServiceCheckInfo;

	public WinServiceCheckInfoRequest getWinServiceCheckInfo() {
		return winServiceCheckInfo;
	}

	public void getWinServiceCheckInfo(WinServiceCheckInfoRequest winServiceCheckInfo) {
		this.winServiceCheckInfo = winServiceCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifyWinserviceMonitorRequest [winServiceCheckInfo=" + winServiceCheckInfo
				+ ", truthValueInfo=" + truthValueInfo + ", application=" + application + ", description=" + description
				+ ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", calendarId=" + calendarId
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + "]";
	}

	
}
