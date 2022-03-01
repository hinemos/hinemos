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

public class AddJmxMasterListRequest implements RequestDto {

	public AddJmxMasterListRequest() {

	}

	private List<JmxMasterInfoRequest> jmxMasterInfoList = new ArrayList<>();

	
	public List<JmxMasterInfoRequest> getJmxMasterInfoList() {
		return jmxMasterInfoList;
	}

	public void setJmxMasterInfoList(List<JmxMasterInfoRequest> jmxMasterInfoList) {
		this.jmxMasterInfoList = jmxMasterInfoList;
	}

	@Override
	public String toString() {
		return "AddJmxMasterListRequest [jmxMasterInfoList=" + jmxMasterInfoList + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
