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
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.HinemosPropertyTypeEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddHinemosPropertyRequest implements RequestDto {

	public AddHinemosPropertyRequest() {
	}

	@RestValidateString(notNull=true, type=CheckType.ID, minLen=1, maxLen=64)
	@RestItemName(value = MessageConstant.HINEMOS_PROPERTY_KEY)
	private String key;
	@RestBeanConvertEnum
	private HinemosPropertyTypeEnum type;
	@RestValidateString(notNull=false, minLen=0)
	@RestItemName(value = MessageConstant.VALUE)
	private String value;
	@RestValidateString(notNull=false, minLen=0)
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public HinemosPropertyTypeEnum getType() {
		return type;
	}

	public void setType(HinemosPropertyTypeEnum type) {
		this.type = type;
	}

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
		return "AddHinemosPropertyRequest [key=" + key + ", type=" + type + ", value=" + value + ", description="
				+ description + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
