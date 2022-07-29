/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyWineventMonitorRequest extends AbstractModifyStringMonitorRequest {

	public ModifyWineventMonitorRequest() {

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
		return "ModifyWineventMonitorRequest [winEventCheckInfo=" + winEventCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + "]";
	}
}
