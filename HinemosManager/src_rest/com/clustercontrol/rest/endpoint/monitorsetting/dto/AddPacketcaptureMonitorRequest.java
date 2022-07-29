/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddPacketcaptureMonitorRequest extends AbstractAddBinaryMonitorRequest {

	public AddPacketcaptureMonitorRequest() {

	}

	private PacketCheckInfoRequest packetCheckInfo;

	public PacketCheckInfoRequest getPacketCheckInfo() {
		return packetCheckInfo;
	}

	public void setPacketCheckInfo(PacketCheckInfoRequest packetCheckInfo) {
		this.packetCheckInfo = packetCheckInfo;
	}

	@Override
	public String toString() {
		return "AddPacketcaptureMonitorRequest [collectorFlg=" + collectorFlg + ", packetCheckInfo=" + packetCheckInfo
				+ ", binaryPatternInfo=" + binaryPatternInfo + ", monitorId=" + monitorId + ", ownerRoleId="
				+ ownerRoleId + ", application=" + application + ", description=" + description + ", monitorFlg="
				+ monitorFlg + ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId="
				+ facilityId + ", notifyRelationList=" + notifyRelationList + "]";
	}

}
