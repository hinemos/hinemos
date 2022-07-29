/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class MakeAttachStorageCommandRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID)
	@RestValidateString(notNull = true)
	private String cloudScopeId;
	@RestItemName(MessageConstant.XCLOUD_CORE_LOCATION_ID)
	@RestValidateString(notNull = true)
	private String locationId;
	@RestItemName(MessageConstant.XCLOUD_CORE_INSTANCE_ID)
	@RestValidateString(notNull = true)
	private String instanceId;
	@RestItemName(MessageConstant.XCLOUD_CORE_STORAGE_ID)
	@RestValidateString(notNull = true)
	private String storageId;
	@RestItemName(MessageConstant.XCLOUD_CORE_OPTIONS)
	private List<OptionRequest> options;
	
	public MakeAttachStorageCommandRequest() {
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
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getStorageId() {
		return storageId;
	}
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	public List<OptionRequest> getOptions() {
		return options;
	}
	public void setOptions(List<OptionRequest> options) {
		this.options = options;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
