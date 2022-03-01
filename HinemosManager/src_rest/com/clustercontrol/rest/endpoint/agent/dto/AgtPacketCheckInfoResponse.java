/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.binary.model.PacketCheckInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = PacketCheckInfo.class)
public class AgtPacketCheckInfoResponse {

	// ---- from MonitorCheckInfo
	private String monitorId;
	private String monitorTypeId;

	// --- from PacketCheckInfo
	// private String monitorId;
	private Boolean promiscuousMode;
	private String filterStr;
	// private MonitorInfo monitorInfo; // 循環参照させない

	public AgtPacketCheckInfoResponse() {
	}

	// ---- accessors
	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

	public Boolean getPromiscuousMode() {
		return promiscuousMode;
	}

	public void setPromiscuousMode(Boolean promiscuousMode) {
		this.promiscuousMode = promiscuousMode;
	}

	public String getFilterStr() {
		return filterStr;
	}

	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
	}

}
