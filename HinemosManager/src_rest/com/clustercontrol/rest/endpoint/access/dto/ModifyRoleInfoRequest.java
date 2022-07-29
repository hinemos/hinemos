/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyRoleInfoRequest implements RequestDto {

	public ModifyRoleInfoRequest() {
	}

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.ROLE_NAME)
	@RestValidateString(notNull = true, maxLen = 128)
	private String roleName;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public String toString() {
		return "ModifyRoleInfoRequest [description=" + description + ", roleName=" + roleName + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
