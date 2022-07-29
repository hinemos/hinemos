/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddCloudserviceBillingMonitorRequest extends AbstractAddNumericMonitorRequest {

	public AddCloudserviceBillingMonitorRequest() {

	}

	private PluginCheckInfoRequest pluginCheckInfo;

	public PluginCheckInfoRequest getPluginCheckInfo() {
		return pluginCheckInfo;
	}

	public void setPluginCheckInfo(PluginCheckInfoRequest pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}

	@Override
	public String toString() {
		return "AddCloudserviceBillingMonitorRequest [pluginCheckInfo=" + pluginCheckInfo + ", collectorFlg="
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
