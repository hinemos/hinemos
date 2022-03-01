/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

public class FacilityBillingResponse {
	private String facilityId;
	private String facilityName;
	private List<DataPointResponse> totalsPerDate;
	private List<ResourceBillingResponse> resources;

	public FacilityBillingResponse() {
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public List<DataPointResponse> getTotalsPerDate() {
		return totalsPerDate;
	}

	public void setTotalsPerDate(List<DataPointResponse> totalsPerDate) {
		this.totalsPerDate = totalsPerDate;
	}

	public List<ResourceBillingResponse> getResources() {
		return resources;
	}

	public void setResources(List<ResourceBillingResponse> resources) {
		this.resources = resources;
	}

}
