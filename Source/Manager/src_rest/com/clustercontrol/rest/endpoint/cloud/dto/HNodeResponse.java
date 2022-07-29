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

import com.clustercontrol.xcloud.bean.HFacility.FacilityType;

public class HNodeResponse {
	private String id;
	private String name;
	private FacilityType type;
	private List<ExtendedPropertyResponse> extendedProperties = new ArrayList<>();

	public HNodeResponse() {
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

	public FacilityType getType() {
		return type;
	}

	public void setType(FacilityType type) {
		this.type = type;
	}

	public List<ExtendedPropertyResponse> getExtendedProperties() {
		return extendedProperties;
	}

	public void setExtendedProperties(List<ExtendedPropertyResponse> extendedProperties) {
		this.extendedProperties = extendedProperties;
	}
}
