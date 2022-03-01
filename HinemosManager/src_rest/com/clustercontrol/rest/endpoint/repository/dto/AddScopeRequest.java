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

public class AddScopeRequest implements RequestDto {

	private String parentFacilityId;

	private ScopeInfoRequest scopeInfo;

	public AddScopeRequest() {
	}

	public String getParentFacilityId() {
		return parentFacilityId;
	}

	public void setParentFacilityId(String parentFacilityId) {
		this.parentFacilityId = parentFacilityId;
	}

	public ScopeInfoRequest getScopeInfo() {
		return scopeInfo;
	}

	public void setScopeInfo(ScopeInfoRequest scopeInfo) {
		this.scopeInfo = scopeInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
