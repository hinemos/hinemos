/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.dto.RequestDto;

public class HasSystemPrivilegeRequest implements RequestDto {

	public HasSystemPrivilegeRequest() {
	}

	@RestValidateCollection(notNull = true)
	private SystemPrivilegeInfoRequestP1 systemPrivilegeInfo;

	public SystemPrivilegeInfoRequestP1 getSystemPrivilegeInfo() {
		return systemPrivilegeInfo;
	}

	public void setSystemPrivilegeInfo(SystemPrivilegeInfoRequestP1 systemPrivilegeInfo) {
		this.systemPrivilegeInfo = systemPrivilegeInfo;
	}

	@Override
	public String toString() {
		return "HasSystemPrivilegeRequest [systemPrivilegeInfo=" + systemPrivilegeInfo + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
