/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

public class AddFilterScopeResponse {

	private ScopeInfoResponseP1 scopeInfo;
	private FacilityInfoResponse[] facilityInfos;

	public AddFilterScopeResponse() {
	}

	public ScopeInfoResponseP1 getScopeInfo() {
		return scopeInfo;
	}

	public void setScopeInfo(ScopeInfoResponseP1 scopeInfo) {
		this.scopeInfo = scopeInfo;
	}

	public FacilityInfoResponse[] getFacilityInfos() {
		return facilityInfos;
	}

	public void setFacilityInfos(FacilityInfoResponse[] facilityInfos) {
		this.facilityInfos = facilityInfos;
	}
}
