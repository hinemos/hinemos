/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.clustercontrol.hub.bean.ValueType;

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

	public static enum KeyType {
		parsing,
		fixed
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
