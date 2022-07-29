/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddLogfileMonitorRequest extends AbstractAddStringMonitorRequest {

	public AddLogfileMonitorRequest() {

	}

	private LogfileCheckInfoRequest logfileCheckInfo;

	public LogfileCheckInfoRequest getLogfileCheckInfo() {
		return logfileCheckInfo;
	}

	public void setLogfileCheckInfo(LogfileCheckInfoRequest logfileCheckInfo) {
		this.logfileCheckInfo = logfileCheckInfo;
	}

	@Override
	public String toString() {
		return "AddLogfileMonitorRequest [logfileCheckInfo=" + logfileCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
