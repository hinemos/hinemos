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

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.xcloud.model.LocationEntity.EntryType;

public class LocationInfoResponse {

	private String id;
	private String locationType;
	@RestPartiallyTransrateTarget
	private String name;
	private EntryType entryType;

	private List<EndpointEntityResponse> endpoints = new ArrayList<>();

	public LocationInfoResponse() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EntryType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryType entryType) {
		this.entryType = entryType;
	}

	public List<EndpointEntityResponse> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<EndpointEntityResponse> endpoints) {
		this.endpoints = endpoints;
	}

}
