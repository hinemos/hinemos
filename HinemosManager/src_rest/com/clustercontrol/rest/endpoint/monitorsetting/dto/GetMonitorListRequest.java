/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class GetMonitorListRequest implements RequestDto {

	public GetMonitorListRequest() {

	}

	private MonitorFilterInfoRequest monitorFilterInfo = new MonitorFilterInfoRequest();

	public MonitorFilterInfoRequest getMonitorFilterInfo() {
		return monitorFilterInfo;
	}

	public void setMonitorFilterInfo(MonitorFilterInfoRequest monitorFilterInfo) {
		this.monitorFilterInfo = monitorFilterInfo;
	}

	@Override
	public String toString() {
		return "GetMonitorListRequest [monitorFilterInfo=" + monitorFilterInfo + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
