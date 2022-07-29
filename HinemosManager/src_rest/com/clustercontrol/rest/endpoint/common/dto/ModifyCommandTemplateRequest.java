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
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyCommandTemplateRequest implements RequestDto {
	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(minLen = -1, maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.COMMAND)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 1024)
	private String command;

	public ModifyCommandTemplateRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
