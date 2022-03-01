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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.xcloud.factory.ICloudOption.PlatformServiceStatus;

public class ModifyPlatformServiceConditionRequest implements RequestDto {

	@RestValidateCollection(minSize = 1, notNull = true)
	private List<String> serviceIdList = new ArrayList<>();
	@RestValidateString(maxLen = 1024)
	private String message;
	@RestValidateObject(notNull = true)
	private PlatformServiceStatus status;
	private String locationId;

	public ModifyPlatformServiceConditionRequest() {
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
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
