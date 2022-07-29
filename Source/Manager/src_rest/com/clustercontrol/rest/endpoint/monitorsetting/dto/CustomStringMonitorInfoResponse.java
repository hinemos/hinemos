/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class CustomStringMonitorInfoResponse extends AbstractStringMonitorResponse {

	private CustomCheckInfoResponse customCheckInfo;

	public CustomStringMonitorInfoResponse() {
	}

	public CustomCheckInfoResponse getCustomCheckInfo() {
		return customCheckInfo;
	}

	public void setCustomCheckInfo(CustomCheckInfoResponse customCheckInfo) {
		this.customCheckInfo = customCheckInfo;
	}

	@Override
	public String toString() {
		return "CustomStringMonitorInfoResponse [customCheckInfo=" + customCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

	
}