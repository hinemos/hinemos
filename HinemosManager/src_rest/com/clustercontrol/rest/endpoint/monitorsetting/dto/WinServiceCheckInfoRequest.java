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

public class WinServiceCheckInfoRequest implements RequestDto {
	private String serviceName;
	public WinServiceCheckInfoRequest() {
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	@Override
	public String toString() {
		return "WinServiceCheckInfo [serviceName=" + serviceName + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}