/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class GetEventInfoRequest implements RequestDto{

	public GetEventInfoRequest(){
	}

	private String monitorId;
	private String monitorDetailId;
	private String pluginId;
	private String facilityId;
	private String outputDate;

	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorDetailId() {
		return this.monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}
	public String getPluginId() {
		return this.pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getOutputDate() {
		return this.outputDate;
	}
	public void setOutputDate(String outputDate) {
		this.outputDate = outputDate;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
