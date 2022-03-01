/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.hub.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.hub.model.LogFormatKey.KeyType;

public class LogFormatKeyRequest implements RequestDto {

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_KEY)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String key;

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_KEY_VALUE_TYPE)
	@RestValidateObject(notNull = true)
	private ValueType valueType;

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_KEY_TYPE)
	@RestValidateObject(notNull = true)
	private KeyType keyType;

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_KEY_PATTERN)
	@RestValidateString(notNull = false)
	private String pattern;

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_KEY_VALUE)
	@RestValidateString(notNull = false)
	private String value;
		
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public KeyType getKeyType() {
		return keyType;
	}
	public void setKeyType(KeyType keyType) {
		this.keyType = keyType;
	}
	
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
