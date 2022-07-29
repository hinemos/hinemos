/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class AddNodeAndAssignScopeFromInstanceRequest implements RequestDto {
	String cloudScopeId;
	String locationId;
	String instanceId;
	public AddNodeAndAssignScopeFromInstanceRequest() {
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

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
	
}
