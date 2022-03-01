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

public class NetworkInfoResponse {
	private String id;
	private String name;
	private String resourceTypeAsPlatform;
	private List<ExtendedPropertyResponse> extendedProperties = new ArrayList<>();
	private List<String> attachedInstances = new ArrayList<>();

	public NetworkInfoResponse() {
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

	public List<String> getAttachedInstances() {
		return attachedInstances;
	}

	public void setAttachedInstances(List<String> attachedInstances) {
		this.attachedInstances = attachedInstances;
	}
}
