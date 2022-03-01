/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;


@RestBeanConvertAssertion(from = PluginCheckInfo.class)
public class AgtPluginCheckInfoResponse {
	private List<AgtMonitorPluginNumericInfoResponse> monitorPluginNumericInfoList = new ArrayList<>();
	private List<AgtMonitorPluginStringInfoResponse> monitorPluginStringInfoList = new ArrayList<>();

	public AgtPluginCheckInfoResponse() {
	}

	public List<AgtMonitorPluginNumericInfoResponse> getMonitorPluginNumericInfoList() {
		return monitorPluginNumericInfoList;
	}
	public void setMonitorPluginNumericInfoList(List<AgtMonitorPluginNumericInfoResponse> monitorPluginNumericInfoList) {
		this.monitorPluginNumericInfoList = monitorPluginNumericInfoList;
	}
	public List<AgtMonitorPluginStringInfoResponse> getMonitorPluginStringInfoList() {
		return monitorPluginStringInfoList;
	}
	public void setMonitorPluginStringInfoList(List<AgtMonitorPluginStringInfoResponse> monitorPluginStringInfoList) {
		this.monitorPluginStringInfoList = monitorPluginStringInfoList;
	}
	@Override
	public String toString() {
		return "PluginCheckInfoResponse [monitorPluginNumericInfoList=" + monitorPluginNumericInfoList
				+ ", monitorPluginStringInfoList=" + monitorPluginStringInfoList + "]";
	}

	
}
