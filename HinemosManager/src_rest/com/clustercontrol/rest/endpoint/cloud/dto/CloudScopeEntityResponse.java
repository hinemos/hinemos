/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.util.FacilityIdUtil;

public class CloudScopeEntityResponse {
	private String platformId;
	private String description;
	@RestPartiallyTransrateTarget
	private String cloudScopeId;
	private String ownerRoleId;
	@RestPartiallyTransrateTarget
	private String name;
	private String accountId;
	private Boolean billingDetailCollectorFlg = false;
	private Integer retentionPeriod;
	@RestBeanConvertDatetime
	private String billingLastDate;
	private Map<String, ExtendedPropertyResponse> extendedProperties = new HashMap<>();
	private Map<String, PrivateLocationResponse> privateLocations = new HashMap<>();
	private List<LocationInfoResponse> locations;
	private CloudLoginUserEntityResponse account;
	@RestBeanConvertDatetime
	private String regDate;
	@RestBeanConvertDatetime
	private String updateDate;
	private String regUser;
	private String updateUser;
	private Boolean isPublic;
	
	public CloudScopeEntityResponse() {
	}
	public String getNodeId() {
		return FacilityIdUtil.getCloudScopeNodeId(getPlatformId(), getCloudScopeId());
	}

	public String getPlatformId() {
		return platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public Boolean getBillingDetailCollectorFlg() {
		return billingDetailCollectorFlg;
	}
	public void setBillingDetailCollectorFlg(Boolean billingDetailCollectorFlg) {
		this.billingDetailCollectorFlg = billingDetailCollectorFlg;
	}
	public Integer getRetentionPeriod() {
		return retentionPeriod;
	}
	public void setRetentionPeriod(Integer retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}
	public String getBillingLastDate() {
		return billingLastDate;
	}
	public void setBillingLastDate(String billingLastDate) {
		this.billingLastDate = billingLastDate;
	}
	public CloudLoginUserEntityResponse getAccount() {
		return account;
	}
	public void setAccount(CloudLoginUserEntityResponse account) {
		this.account = account;
	}
	public List<ExtendedPropertyResponse> getExtendedProperties() {
		List<ExtendedPropertyResponse> properties = new ArrayList<>();
		for (ExtendedPropertyResponse peoperty: extendedProperties.values()) {
			properties.add(peoperty);
		}
		return properties;
	}
	public void setExtendedProperties(Map<String, ExtendedPropertyResponse> extendedProperties) {
		this.extendedProperties = extendedProperties;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	public Map<String, PrivateLocationResponse> getPrivateLocations() {
		return privateLocations;
	}
	public void setPrivateLocations(Map<String, PrivateLocationResponse> privateLocations) {
		this.privateLocations = privateLocations;
	}
	public List<LocationInfoResponse> getLocations() {
		if (!isPublic) {
			locations = new ArrayList<>();
			for (PrivateLocationResponse priLocEnt : privateLocations.values()) {
				LocationInfoResponse location = new LocationInfoResponse();
				location.setId(priLocEnt.getLocationId());
				location.setName(priLocEnt.getName());
				location.setEntryType(LocationEntity.EntryType.user);
				location.setLocationType(platformId);
				for (PrivateEndpointReponse priEndEnt: priLocEnt.getEndpoints()) {
					EndpointEntityResponse endpoint = new EndpointEntityResponse();
					endpoint.setEndpointId(priEndEnt.getEndpointId());
					endpoint.setUrl(priEndEnt.getUrl());
					location.getEndpoints().add(endpoint);
				}
				locations.add(location);
			}
		}
		return locations;
	}
	public void setLocations(List<LocationInfoResponse> locations) {
		this.locations = locations;
	}
	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}
}
