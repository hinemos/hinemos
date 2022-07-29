/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class RpaLogfileMonitorInfoResponse extends AbstractStringMonitorResponse {
	public RpaLogfileMonitorInfoResponse() {
	}

	private RpaLogFileCheckInfoResponse rpaLogFileCheckInfo;

	public RpaLogFileCheckInfoResponse getRpaLogFileCheckInfo() {
		return rpaLogFileCheckInfo;
	}

	public void setRpaLogFileCheckInfo(RpaLogFileCheckInfoResponse rpaLogFileCheckInfo) {
		this.rpaLogFileCheckInfo = rpaLogFileCheckInfo;
	}

	@Override
	public String toString() {
		return "RpaLogfileCheckInfoResponse [RpalogFileCheckInfo=" + rpaLogFileCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}
}
