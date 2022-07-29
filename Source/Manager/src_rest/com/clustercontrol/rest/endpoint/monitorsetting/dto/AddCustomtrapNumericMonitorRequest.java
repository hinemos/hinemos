/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddCustomtrapNumericMonitorRequest extends AbstractAddNumericMonitorRequest {

	public AddCustomtrapNumericMonitorRequest() {

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
		return "AddCustomtrapNumericMonitorRequest [customTrapCheckInfo=" + customTrapCheckInfo + ", collectorFlg="
				+ collectorFlg + ", itemName=" + itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg
				+ ", predictionMethod=" + predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange
				+ ", predictionTarget=" + predictionTarget + ", predictionApplication=" + predictionApplication
				+ ", changeFlg=" + changeFlg + ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication="
				+ changeApplication + ", numericValueInfo=" + numericValueInfo + ", predictionNotifyRelationList="
				+ predictionNotifyRelationList + ", changeNotifyRelationList=" + changeNotifyRelationList
				+ ", monitorId=" + monitorId + ", application=" + application + ", description=" + description
				+ ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", calendarId=" + calendarId
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}
}
