/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;

public class AddRpaLogfileMonitorRequest extends AbstractAddStringMonitorRequest {

	public AddRpaLogfileMonitorRequest() {

	}

	@RestValidateObject(notNull=true)
	private RpaLogFileCheckInfoRequest rpaLogFileCheckInfo;

	public RpaLogFileCheckInfoRequest getRpaLogFileCheckInfo() {
		return rpaLogFileCheckInfo;
	}

	public void setLogFileCheckInfo(RpaLogFileCheckInfoRequest rpalogFileCheckInfo) {
		this.rpaLogFileCheckInfo = rpalogFileCheckInfo;
	}

	@Override
	public String toString() {
		return "AddRpaLogfileMonitorRequest [RpaLogFileCheckInfo=" + rpaLogFileCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		rpaLogFileCheckInfo.correlationCheck();
	}

}
