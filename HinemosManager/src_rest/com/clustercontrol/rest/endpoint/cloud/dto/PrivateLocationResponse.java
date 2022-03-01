/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

public class PrivateLocationResponse {
	private String locationId;
	private String name;
	private List<PrivateEndpointReponse> endpoints = new ArrayList<>();
	public PrivateLocationResponse() {
	}
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PrivateEndpointReponse> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<PrivateEndpointReponse> endpoints) {
		this.endpoints = endpoints;
	}
}
