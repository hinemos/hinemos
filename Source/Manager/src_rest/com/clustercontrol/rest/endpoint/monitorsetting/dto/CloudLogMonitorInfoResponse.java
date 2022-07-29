/*

 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class CloudLogMonitorInfoResponse extends AbstractStringMonitorResponse {

	private PluginCheckInfoResponse pluginCheckInfo;

	public CloudLogMonitorInfoResponse() {
	}

	public PluginCheckInfoResponse getPluginCheckInfo() {
		return pluginCheckInfo;
	}

	public void setPluginCheckInfo(PluginCheckInfoResponse pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}

	@Override
	public String toString() {
		return "CloudserviceMonitorInfoResponse [pluginCheckInfo=" + pluginCheckInfo + ", stringValueInfo="
				+ stringValueInfo + ", monitorId=" + monitorId + ", application=" + application + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", facilityId="
				+ facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

	
}