/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.hub.dto;

import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.hub.model.LogFormatKey.KeyType;

public class LogFormatKeyResponse {
	private String key;
	private String description;
	private ValueType valueType;
	private KeyType keyType;
	private String pattern;
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
}
