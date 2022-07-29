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

public class GetAgentValidManagerFacilityIdsResponse {

	private List<String> facilityIds = new ArrayList<>();

	public GetAgentValidManagerFacilityIdsResponse() {
	}

	public List<String> getFacilityIds() {
		return facilityIds;
	}

	public void setFacilityIds(List<String> facilityIds) {
		this.facilityIds = facilityIds;
	}
}
