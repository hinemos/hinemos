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

public class ReplaceNodeVariableRequest implements RequestDto {

	private String facilityId;
	private String replaceObject;

	public ReplaceNodeVariableRequest() {
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getReplaceObject() {
		return replaceObject;
	}

	public void setReplaceObject(String replaceObject) {
		this.replaceObject = replaceObject;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
