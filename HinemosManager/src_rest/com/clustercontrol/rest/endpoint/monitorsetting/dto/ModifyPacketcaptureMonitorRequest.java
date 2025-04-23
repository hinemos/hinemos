/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"runInterval"})
public class ModifyPacketcaptureMonitorRequest extends AbstractModifyBinaryMonitorRequest {

	public ModifyPacketcaptureMonitorRequest() {
		// runIntervalは、固定値にする
		super.setRunInterval(RunIntervalEnum.NONE);
	}

	private PacketCheckInfoRequest packetCheckInfo;

	@Override
	public void setRunInterval(RunIntervalEnum runInterval) {
	}

	public PacketCheckInfoRequest getPacketCheckInfo() {
		return packetCheckInfo;
	}

	public void setPacketCheckInfo(PacketCheckInfoRequest packetCheckInfo) {
		this.packetCheckInfo = packetCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifyPacketcaptureMonitorRequest [collectorFlg=" + collectorFlg + ", packetCheckInfo="
				+ packetCheckInfo + ", binaryPatternInfo=" + binaryPatternInfo + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + "]";
	}
}
