/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.HashMap;
import java.util.Map;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.ErrorCode;

public class PublicLocationEntity {
	private String id;
	private String locationType;
	private String name;
	
	private Map<String, PublicEndpointEntity> endpoints = new HashMap<>();

	public PublicLocationEntity() {
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getLocationType() {
		return locationType;
	}
	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public Map<String, PublicEndpointEntity> getEndpoints() {
		return this.endpoints;
	}
	public void setEndpoints(Map<String, PublicEndpointEntity> endpoints) {
		this.endpoints = endpoints;
	}

	public PublicEndpointEntity getEndpoint(String endpointId) throws CloudManagerException {
		PublicEndpointEntity endpoint = getEndpoints().get(endpointId);
		if (endpoint == null)
			throw ErrorCode.ENDPOINT_INVALID_ENDPOINT_FOUND.cloudManagerFault(id, endpointId);
		return endpoint;
	}
	
	public LocationEntity toLocationEntity(PublicCloudScopeEntity cloudScope) {
		LocationEntity location = new LocationEntity();
		location.setCloudScope(cloudScope);
		location.setLocationId(this.getId());
		location.setName(this.getName());
		location.setEntryType(LocationEntity.EntryType.cloud);
		location.setLocationType(this.getLocationType());
		for (PublicEndpointEntity endpoint: this.getEndpoints().values()) {
			location.getEndpoints().put(endpoint.getId(), endpoint.toEndpointEntity(location));
		}
		return location;
	}
}
