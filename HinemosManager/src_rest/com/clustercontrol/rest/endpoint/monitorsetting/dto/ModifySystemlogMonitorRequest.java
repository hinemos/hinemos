/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifySystemlogMonitorRequest extends AbstractModifyStringMonitorRequest {

	public ModifySystemlogMonitorRequest() {

	}

	@Override
	public String toString() {
		return "ModifySystemlogMonitorRequest [collectorFlg=" + collectorFlg + ", logFormatId=" + logFormatId
				+ ", stringValueInfo=" + stringValueInfo + ", application=" + application + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", calendarId="
				+ calendarId + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + "]";
	}
}
