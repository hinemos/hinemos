/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyBinaryfileMonitorRequest extends AbstractModifyBinaryMonitorRequest {

	public ModifyBinaryfileMonitorRequest() {
	}

	private BinaryCheckInfoRequest binaryCheckInfo;

	public BinaryCheckInfoRequest getBinaryCheckInfo() {
		return binaryCheckInfo;
	}

	public void setBinaryCheckInfo(BinaryCheckInfoRequest binaryCheckInfo) {
		this.binaryCheckInfo = binaryCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifyBinaryfileMonitorRequest [collectorFlg=" + collectorFlg + ", binaryCheckInfo=" + binaryCheckInfo
				+ ", binaryPatternInfo=" + binaryPatternInfo + ", application=" + application + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", calendarId="
				+ calendarId + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + "]";
	}
}
