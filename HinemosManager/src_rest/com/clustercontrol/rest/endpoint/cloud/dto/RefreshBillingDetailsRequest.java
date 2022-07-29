/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class RefreshBillingDetailsRequest implements RequestDto {
	private String cloudScopeId;

	public RefreshBillingDetailsRequest() {
	}

	public String getCloudScopeId() {
		return cloudScopeId;
	}

	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
	
}
