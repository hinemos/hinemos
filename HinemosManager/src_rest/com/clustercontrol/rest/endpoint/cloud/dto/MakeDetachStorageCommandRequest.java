/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class MakeDetachStorageCommandRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID)
	@RestValidateString(notNull = true)
	private String cloudScopeId;
	@RestItemName(MessageConstant.XCLOUD_CORE_LOCATION_ID)
	@RestValidateString(notNull = true)
	private String locationId;
	@RestItemName(MessageConstant.XCLOUD_CORE_STORAGE_ID)
	@RestValidateString(notNull = true)
	private String storageId;
	
	public MakeDetachStorageCommandRequest() {
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
	
	public String getStorageId() {
		return storageId;
	}
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
