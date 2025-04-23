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
public class ModifyCustomtrapStringMonitorRequest extends AbstractModifyStringMonitorRequest {

	public ModifyCustomtrapStringMonitorRequest() {
		// runIntervalは、固定値にする
		super.setRunInterval(RunIntervalEnum.NONE);
	}

	private CustomTrapCheckInfoRequest customTrapCheckInfo;

	@Override
	public void setRunInterval(RunIntervalEnum runInterval) {
	}
	
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
