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
import com.clustercontrol.xcloud.bean.InstanceStatus;
import com.clustercontrol.xcloud.factory.IResourceManagement.Instance.Platform;

public class InstanceEntityResponse {
	@RestBeanConvertDatetime
	private String regDate;
	@RestBeanConvertDatetime
	private String updateDate;
	private String regUser;
	private String updateUser;
	private String cloudScopeId;
	private String locationId;
	private String resourceId;
	private String name;
	private String facilityId;
	private Platform platform;
	private InstanceStatus instanceStatus;
	private List<String> ipAddresses = new ArrayList<>();
	private String instanceStatusAsPlatform;
	private String resourceTypeAsPlatform;
	private String memo;
	private Map<String, ResourceTagResponse> tags = new HashMap<>();

	public InstanceEntityResponse() {
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

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Platform getPlatform() {
		return platform;
	}

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	public InstanceStatus getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(InstanceStatus instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	public List<String> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(List<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public String getInstanceStatusAsPlatform() {
		return instanceStatusAsPlatform;
	}

	public void setInstanceStatusAsPlatform(String instanceStatusAsPlatform) {
		this.instanceStatusAsPlatform = instanceStatusAsPlatform;
	}

	public String getResourceTypeAsPlatform() {
		return resourceTypeAsPlatform;
	}

	public void setResourceTypeAsPlatform(String resourceTypeAsPlatform) {
		this.resourceTypeAsPlatform = resourceTypeAsPlatform;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Map<String, ResourceTagResponse> getTags() {
		return tags;
	}

	public void setTags(Map<String, ResourceTagResponse> tags) {
		this.tags = tags;
	}
}
