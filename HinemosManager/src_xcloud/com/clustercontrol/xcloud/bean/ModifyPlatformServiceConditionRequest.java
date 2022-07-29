/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.xcloud.factory.ICloudOption.PlatformServiceStatus;

public class ModifyPlatformServiceConditionRequest extends Request {
	private String cloudScopeId;
	private String locationId;
	private List<String> serviceIdList = new ArrayList<>();
	private String message;
	private PlatformServiceStatus status;
	
	public ModifyPlatformServiceConditionRequest() {
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
	public List<String> getServiceIdList() {
		return serviceIdList;
	}
	public void setServiceIdList(List<String> serviceIdList) {
		this.serviceIdList = serviceIdList;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public PlatformServiceStatus getStatus() {
		return status;
	}
	public void setStatus(PlatformServiceStatus status) {
		this.status = status;
	}
}
