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
import com.clustercontrol.xcloud.bean.HFacility.FacilityType;

public class HFacilityResponse {
	private String id;
	@RestPartiallyTransrateTarget
	private String name;
	private FacilityType type;
	private List<ExtendedPropertyResponse> extendedProperties = new ArrayList<>();
	private List<HFacilityResponse> facilities = new ArrayList<>();
	private CloudScopeInfoResponse cloudScope;
	private LocationInfoResponse location;
	private String folderType;
	private InstanceInfoResponse instance;
	private String entityType;

	/**
	 * オブジェクトの親クラウドスコープID
	 */
	private String parentCloudScopeId;
	/**
	 * オブジェクトの親クラウドスコープのプラットフォーム
	 */
	private String platformId;

	public HFacilityResponse() {
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

	public List<HFacilityResponse> getFacilities() {
		return facilities;
	}

	public void setFacilities(List<HFacilityResponse> facilities) {
		this.facilities = facilities;
	}

	public CloudScopeInfoResponse getCloudScope() {
		return cloudScope;
	}

	public void setCloudScope(CloudScopeInfoResponse cloudScope) {
		this.cloudScope = cloudScope;
	}

	public LocationInfoResponse getLocation() {
		return location;
	}

	public void setLocation(LocationInfoResponse location) {
		this.location = location;
	}

	public String getFolderType() {
		return folderType;
	}

	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}

	public InstanceInfoResponse getInstance() {
		return instance;
	}

	public void setInstance(InstanceInfoResponse instance) {
		this.instance = instance;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getParentCloudScopeId() {
		return parentCloudScopeId;
	}

	public void setParentCloudScopeId(String parentCloudScopeId) {
		this.parentCloudScopeId = parentCloudScopeId;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}
}
