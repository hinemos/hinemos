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

public class ModifyCloudScopeRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_NAME)
	@RestValidateString(maxLen = 256)
	private String scopeName;
	@RestItemName(MessageConstant.XCLOUD_CORE_DESCRIPTION)
	@RestValidateString(notNull = true)
	private String description;
	@RestItemName(MessageConstant.XCLOUD_CORE_PRIVATE_LOCATIONS)
	private List<PrivateLocationRequest> privateLocations;

	public ModifyCloudScopeRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	public String getScopeName() {
		return scopeName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PrivateLocationRequest> getPrivateLocations() {
		return privateLocations;
	}

	public void setPrivateLocations(List<PrivateLocationRequest> privateLocations) {
		this.privateLocations = privateLocations;
	}

}
