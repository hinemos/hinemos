/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

public class IsNodeResponse {

	private String facilityId;
	private Boolean isNode;

	public IsNodeResponse() {
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Boolean getIsNode() {
		return isNode;
	}

	public void setIsNode(Boolean isNode) {
		this.isNode = isNode;
	}
}
