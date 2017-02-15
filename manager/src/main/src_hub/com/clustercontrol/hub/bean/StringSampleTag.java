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
package com.clustercontrol.hub.bean;

import java.io.Serializable;

/**
 * StringSample に紐づけるタグ情報を格納。
 *
 */
public class StringSampleTag implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String key;
	private ValueType type;
	private String value;
	
	public StringSampleTag() {
	}
	
	public StringSampleTag(String key, ValueType type, String value){
		this.key = key;
		this.type = type;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public ValueType getType() {
		return type;
	}
	public void setType(ValueType type) {
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}