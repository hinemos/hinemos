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

public class LocationEntity {
	public static enum EntryType {
		cloud,
		user
	}
	
	private CloudScopeEntity cloudScope;
	private String id;
	private String locationType;
	private String name;
	private EntryType entryType;
	
	private Map<String, EndpointEntity> endpoints = new HashMap<>();

	public LocationEntity() {
	}

	public CloudScopeEntity getCloudScope() {
		return cloudScope;
	}
	public void setCloudScope(CloudScopeEntity cloudScope) {
		this.cloudScope = cloudScope;
	}

	public String getLocationId() {
		return id;
	}
	public void setLocationId(String id) {
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

	public EntryType getEntryType() {
		return entryType;
	}
	public void setEntryType(EntryType entryType) {
		this.entryType = entryType;
	}

	public Map<String, EndpointEntity> getEndpoints() {
		return this.endpoints;
	}
	public void setEndpoints(Map<String, EndpointEntity> endpoints) {
		this.endpoints = endpoints;
	}

	public EndpointEntity getEndpoint(String endpointId) throws CloudManagerException {
		EndpointEntity endpoint = getEndpoints().get(endpointId);
		if (endpoint == null)
			throw ErrorCode.ENDPOINT_INVALID_ENDPOINT_FOUND.cloudManagerFault(id, endpointId);
		return endpoint;
	}
}
