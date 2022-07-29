/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class PacketCheckInfoRequest implements RequestDto {
	private Boolean promiscuousMode;
	private String filterStr;
	public PacketCheckInfoRequest() {
	}
	public Boolean isPromiscuousMode() {
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
	@Override
	public String toString() {
		return "PacketCheckInfo [promiscuousMode="
				+ promiscuousMode + ", filterStr=" + filterStr + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}