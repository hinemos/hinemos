/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.dto.RequestDto;

public class ReplaceSystemPrivilegeWithRoleRequest implements RequestDto {

	public ReplaceSystemPrivilegeWithRoleRequest() {
	}

	@RestValidateCollection(notNull = true, minSize = 1)
	private List<SystemPrivilegeInfoRequestP1> systemPrivilegeList;

	public List<SystemPrivilegeInfoRequestP1> getSystemPrivilegeList() {
		return systemPrivilegeList;
	}

	public void setSystemPrivilegeList(List<SystemPrivilegeInfoRequestP1> systemPrivilegeList) {
		this.systemPrivilegeList = systemPrivilegeList;
	}

	@Override
	public String toString() {
		return "ReplaceSystemPrivilegeWithRoleRequest [systemPrivilegeList=" + systemPrivilegeList + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
