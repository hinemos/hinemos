/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

public class PluginCheckInfoResponse {
	private List<MonitorPluginNumericInfoResponse> monitorPluginNumericInfoList = new ArrayList<>();
	private List<MonitorPluginStringInfoResponse> monitorPluginStringInfoList = new ArrayList<>();

	public PluginCheckInfoResponse() {
	}

	public List<MonitorPluginNumericInfoResponse> getMonitorPluginNumericInfoList() {
		return monitorPluginNumericInfoList;
	}
	public void setMonitorPluginNumericInfoList(List<MonitorPluginNumericInfoResponse> monitorPluginNumericInfoList) {
		this.monitorPluginNumericInfoList = monitorPluginNumericInfoList;
	}
	public List<MonitorPluginStringInfoResponse> getMonitorPluginStringInfoList() {
		return monitorPluginStringInfoList;
	}
	public void setMonitorPluginStringInfoList(List<MonitorPluginStringInfoResponse> monitorPluginStringInfoList) {
		this.monitorPluginStringInfoList = monitorPluginStringInfoList;
	}
	@Override
	public String toString() {
		return "PluginCheckInfoResponse [monitorPluginNumericInfoList=" + monitorPluginNumericInfoList
				+ ", monitorPluginStringInfoList=" + monitorPluginStringInfoList + "]";
	}

	
}
