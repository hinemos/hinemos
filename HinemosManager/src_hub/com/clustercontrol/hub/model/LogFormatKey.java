/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.rest.dto.EnumDto;

/**
 * The persistent class for the cc_log_format_key database table.
 * 
 */
@Embeddable
public class LogFormatKey implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum KeyType implements EnumDto<Integer> {
		parsing,
		fixed;

		@Override
		public Integer getCode() {
			return this.ordinal();
		}
	}

	private String key;
	private String description;
	private ValueType valueType;
	private KeyType keyType;
	private String pattern;
	private String value;
	
	public LogFormatKey(){
	}
	
	@Column(name="property_key")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name="value_type")
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(name="key_type")
	public KeyType getKeyType() {
		return keyType;
	}
	public void setKeyType(KeyType keyType) {
		this.keyType = keyType;
	}
	
	@Column(name="pattern")
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	@Column(name="property_value")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
