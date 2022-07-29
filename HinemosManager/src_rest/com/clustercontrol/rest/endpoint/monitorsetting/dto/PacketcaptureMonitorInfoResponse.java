/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class PacketcaptureMonitorInfoResponse extends AbstractBinaryMonitorInfoResponse {

	public PacketcaptureMonitorInfoResponse() {
	}

	private PacketCheckInfoResponse packetCheckInfo;

	public PacketCheckInfoResponse getPacketCheckInfo() {
		return packetCheckInfo;
	}
	public void setPacketCheckInfo(PacketCheckInfoResponse packetCheckInfo) {
		this.packetCheckInfo = packetCheckInfo;
	}
	@Override
	public String toString() {
		return "PacketcaptureMonitorInfoResponse [collectorFlg=" + collectorFlg + ", packetCheckInfo=" + packetCheckInfo
				+ ", binaryPatternInfo=" + binaryPatternInfo + ", monitorId=" + monitorId + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList
				+ ", ownerRoleId=" + ownerRoleId + "]";
	}

	
}