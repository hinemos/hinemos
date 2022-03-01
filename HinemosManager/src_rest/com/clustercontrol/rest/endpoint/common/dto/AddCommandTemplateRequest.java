/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddCommandTemplateRequest implements RequestDto {
	@RestItemName(value = MessageConstant.COMMAND_TEMPLATE_ID)
	@RestValidateString(type = CheckType.ID, notNull = true, minLen = 1, maxLen = 64)
	private String commandTemplateId;

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(minLen = -1, maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true)
	private String ownerRoleId;

	@RestItemName(value = MessageConstant.COMMAND)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 1024)
	private String command;

	public AddCommandTemplateRequest() {
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	public String getCommandTemplateId() {
		return commandTemplateId;
	}

	public void setCommandTemplateId(String commandTemplateId) {
		this.commandTemplateId = commandTemplateId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
