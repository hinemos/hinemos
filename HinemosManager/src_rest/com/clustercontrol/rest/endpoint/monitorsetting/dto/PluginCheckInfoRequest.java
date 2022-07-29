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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class PluginCheckInfoRequest implements RequestDto {
	private List<MonitorPluginNumericInfoRequest> monitorPluginNumericInfoList = new ArrayList<>();
	private List<MonitorPluginStringInfoRequest> monitorPluginStringInfoList = new ArrayList<>();
	public PluginCheckInfoRequest() {
	}

	
	
	public List<MonitorPluginNumericInfoRequest> getMonitorPluginNumericInfoList() {
		return monitorPluginNumericInfoList;
	}



	public void setMonitorPluginNumericInfoList(List<MonitorPluginNumericInfoRequest> monitorPluginNumericInfoList) {
		this.monitorPluginNumericInfoList = monitorPluginNumericInfoList;
	}



	public List<MonitorPluginStringInfoRequest> getMonitorPluginStringInfoList() {
		return monitorPluginStringInfoList;
	}



	public void setMonitorPluginStringInfoList(List<MonitorPluginStringInfoRequest> monitorPluginStringInfoList) {
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
