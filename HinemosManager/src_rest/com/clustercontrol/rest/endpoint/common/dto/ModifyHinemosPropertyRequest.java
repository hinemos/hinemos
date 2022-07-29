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

public class ModifyHinemosPropertyRequest implements RequestDto {

	public ModifyHinemosPropertyRequest() {
	}

	@RestItemName(value = MessageConstant.VALUE)
	@RestValidateString(notNull=false, minLen=0)
	private String value;
	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(notNull=false, minLen=0)
	private String description;

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "ModifyHinemosPropertyRequest [value=" + value + ", description=" + description + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
