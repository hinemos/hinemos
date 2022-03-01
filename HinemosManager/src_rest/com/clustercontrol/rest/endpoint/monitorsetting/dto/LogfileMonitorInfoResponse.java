/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class LogfileMonitorInfoResponse extends AbstractStringMonitorResponse {

	private LogfileCheckInfoResponse logfileCheckInfo;

	public LogfileMonitorInfoResponse() {
	}

	public LogfileCheckInfoResponse getLogfileCheckInfo() {
		return logfileCheckInfo;
	}

	public void setLogfileCheckInfo(LogfileCheckInfoResponse logfileCheckInfo) {
		this.logfileCheckInfo = logfileCheckInfo;
	}

	@Override
	public String toString() {
		return "LogfileMonitorInfoResponse [logfileCheckInfo=" + logfileCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

	
}