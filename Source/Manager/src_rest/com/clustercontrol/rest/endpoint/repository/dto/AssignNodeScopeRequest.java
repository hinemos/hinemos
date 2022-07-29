/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class AssignNodeScopeRequest implements RequestDto {

	List<String> facilityIdList = new ArrayList<>();

	public AssignNodeScopeRequest() {
	}

	public List<String> getFacilityIdList() {
		return facilityIdList;
	}

	public void setFacilityIdList(List<String> facilityIdList) {
		this.facilityIdList = facilityIdList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
