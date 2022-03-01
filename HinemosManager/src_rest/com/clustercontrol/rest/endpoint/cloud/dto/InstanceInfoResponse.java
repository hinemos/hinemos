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

import com.clustercontrol.xcloud.bean.LocationResource.ResourceType;

public class InstanceInfoResponse {
	private String cloudScopeId;
	private String locationId;
	private String id;
	private String name;
	private ResourceType resourceType;
	private String resourceTypeAsPlatform;
	private List<ExtendedPropertyResponse> extendedProperties = new ArrayList<>();
	private InstanceEntityResponse entity;

	private InstanceBackupEntryResponse backup;

	public InstanceInfoResponse() {
	}

	public String getCloudScopeId() {
		return cloudScopeId;
	}

	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
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

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceTypeAsPlatform() {
		return resourceTypeAsPlatform;
	}

	public void setResourceTypeAsPlatform(String resourceTypeAsPlatform) {
		this.resourceTypeAsPlatform = resourceTypeAsPlatform;
	}

	public List<ExtendedPropertyResponse> getExtendedProperties() {
		return extendedProperties;
	}

	public void setExtendedProperties(List<ExtendedPropertyResponse> extendedProperties) {
		this.extendedProperties = extendedProperties;
	}

	public InstanceEntityResponse getEntity() {
		return entity;
	}

	public void setEntity(InstanceEntityResponse entity) {
		this.entity = entity;
	}

	public InstanceBackupEntryResponse getBackup() {
		return backup;
	}

	public void setBackup(InstanceBackupEntryResponse backup) {
		this.backup = backup;
	}
}
