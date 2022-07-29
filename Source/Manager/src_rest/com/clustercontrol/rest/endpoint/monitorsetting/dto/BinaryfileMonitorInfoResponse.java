/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class BinaryfileMonitorInfoResponse extends AbstractBinaryMonitorInfoResponse {

	private BinaryCheckInfoResponse binaryCheckInfo;

	public BinaryfileMonitorInfoResponse() {
	}

	public BinaryCheckInfoResponse getBinaryCheckInfo() {
		return binaryCheckInfo;
	}
	public void setBinaryCheckInfo(BinaryCheckInfoResponse binaryCheckInfo) {
		this.binaryCheckInfo = binaryCheckInfo;
	}
	@Override
	public String toString() {
		return "BinaryfileMonitorInfoResponse [collectorFlg=" + collectorFlg + ", binaryCheckInfo=" + binaryCheckInfo
				+ ", binaryPatternInfo=" + binaryPatternInfo + ", monitorId=" + monitorId + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList
				+ ", ownerRoleId=" + ownerRoleId + "]";
	}

}