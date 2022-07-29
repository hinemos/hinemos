/*
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

public class SetStatusCollectorRequest implements RequestDto {

	public SetStatusCollectorRequest() {

	}

	private List<String> monitorIds = new ArrayList<>();
	private Boolean validFlg;

	
	public List<String> getMonitorIds() {
		return monitorIds;
	}


	public void setMonitorIds(List<String> monitorIds) {
		this.monitorIds = monitorIds;
	}


	public Boolean getValidFlg() {
		return validFlg;
	}


	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}


	@Override
	public String toString() {
		return "SetStatusMonitorRequest [monitorIds=" + monitorIds + ", validFlg=" + validFlg + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
