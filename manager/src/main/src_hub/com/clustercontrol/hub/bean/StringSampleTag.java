/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
	public StringSampleTag(CollectStringTag tag, String value){
		if (tag != null) {
			this.key = tag.name();
			this.type = tag.valueType();
		}
		this.value = value;
	}

	public void setTag(CollectStringTag tag) {
		if (tag != null) {
			this.key = tag.name();
			this.type = tag.valueType();
		}
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