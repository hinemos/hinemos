/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyLogfileMonitorRequest extends AbstractModifyStringMonitorRequest {

	public ModifyLogfileMonitorRequest() {

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
		return "ModifyLogfileMonitorRequest [logfileCheckInfo=" + logfileCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + "]";
	}
}
