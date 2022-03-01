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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = PluginCheckInfo.class)
public class AgtPluginCheckInfoRequest extends AgentRequestDto {
	private List<AgtMonitorPluginNumericInfoRequest> monitorPluginNumericInfoList = new ArrayList<>();
	private List<AgtMonitorPluginStringInfoRequest> monitorPluginStringInfoList = new ArrayList<>();
	public AgtPluginCheckInfoRequest() {
	}

	
	
	public List<AgtMonitorPluginNumericInfoRequest> getMonitorPluginNumericInfoList() {
		return monitorPluginNumericInfoList;
	}



	public void setMonitorPluginNumericInfoList(List<AgtMonitorPluginNumericInfoRequest> monitorPluginNumericInfoList) {
		this.monitorPluginNumericInfoList = monitorPluginNumericInfoList;
	}



	public List<AgtMonitorPluginStringInfoRequest> getMonitorPluginStringInfoList() {
		return monitorPluginStringInfoList;
	}



	public void setMonitorPluginStringInfoList(List<AgtMonitorPluginStringInfoRequest> monitorPluginStringInfoList) {
		this.monitorPluginStringInfoList = monitorPluginStringInfoList;
	}



	@Override
	public String toString() {
		return "PluginCheckInfoRequest [monitorPluginNumericInfoList=" + monitorPluginNumericInfoList
				+ ", monitorPluginStringInfoList=" + monitorPluginStringInfoList + "]";
	}



	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
