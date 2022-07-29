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
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class PrivateLocationRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_LOCATION_ID)
	@RestValidateString(notNull = true, minLen = 1, type = CheckType.ID)
	private String locationId;
	@RestItemName(MessageConstant.XCLOUD_CORE_LOCATION_NAME)
	@RestValidateString(notNull = true, maxLen=256)
	private String name;
	@RestItemName(MessageConstant.XCLOUD_CORE_ENDPOINTS)
	@RestValidateObject(notNull = true)
	private List<PrivateEndpointRequest> endpoints = new ArrayList<>();
	
	public PrivateLocationRequest() {
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PrivateEndpointRequest> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<PrivateEndpointRequest> endpoints) {
		this.endpoints = endpoints;
	}
}
