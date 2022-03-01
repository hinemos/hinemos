/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyCustomtrapStringMonitorRequest extends AbstractModifyStringMonitorRequest {

	public ModifyCustomtrapStringMonitorRequest() {

	}

	private CustomTrapCheckInfoRequest customTrapCheckInfo;

	
	public CustomTrapCheckInfoRequest getCustomTrapCheckInfo() {
		return customTrapCheckInfo;
	}


	public void setCustomTrapCheckInfo(CustomTrapCheckInfoRequest customTrapCheckInfo) {
		this.customTrapCheckInfo = customTrapCheckInfo;
	}


	@Override
	public String toString() {
		return "ModifyCustomtrapStringMonitorRequest [customTrapCheckInfo=" + customTrapCheckInfo + ", collectorFlg="
				+ collectorFlg + ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", priorityChangeJudgmentType=" + priorityChangeJudgmentType + ", notifyRelationList="
				+ notifyRelationList + "]";
	}
}
