/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"runInterval"})
public class AddSystemlogMonitorRequest extends AbstractAddStringMonitorRequest {

	public AddSystemlogMonitorRequest() {
		// runIntervalは、固定値にする
		super.setRunInterval(RunIntervalEnum.NONE);
	}

	@Override
	public void setRunInterval(RunIntervalEnum runInterval) {
	}

	@Override
	public String toString() {
		return "AddSystemlogMonitorRequest [collectorFlg=" + collectorFlg + ", logFormatId=" + logFormatId
				+ ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
