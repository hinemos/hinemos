/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class CustomtrapStringMonitorInfoResponse extends AbstractStringMonitorResponse {

	private CustomTrapCheckInfoResponse customTrapCheckInfo;

	public CustomtrapStringMonitorInfoResponse() {
	}

	public CustomTrapCheckInfoResponse getCustomTrapCheckInfo() {
		return customTrapCheckInfo;
	}

	public void setCustomTrapCheckInfo(CustomTrapCheckInfoResponse customTrapCheckInfo) {
		this.customTrapCheckInfo = customTrapCheckInfo;
	}

	@Override
	public String toString() {
		return "CustomtrapStringMonitorInfoResponse [customTrapCheckInfo=" + customTrapCheckInfo + ", collectorFlg="
				+ collectorFlg + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList
				+ ", ownerRoleId=" + ownerRoleId + "]";
	}

	
}