/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyCloudLogMonitorRequest extends AbstractModifyStringMonitorRequest {

	public ModifyCloudLogMonitorRequest() {

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
		return "ModifyCloudLogMonitorRequest [pluginCheckInfo=" + pluginCheckInfo + ", collectorFlg="
				+ collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + "]";
	}
}
