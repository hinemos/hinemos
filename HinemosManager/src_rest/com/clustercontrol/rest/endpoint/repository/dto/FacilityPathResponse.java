/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class FacilityPathResponse {

	@RestPartiallyTransrateTarget
	private String facilityPath;

	public FacilityPathResponse() {
	}

	public String getFacilityPath() {
		return facilityPath;
	}

	public void setFacilityPath(String facilityPath) {
		this.facilityPath = facilityPath;
	}
}
