/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;



public class ExtendedProperty {
	private String name;
	private String value;
	
	public ExtendedProperty() {
	}
	public ExtendedProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public static ExtendedProperty convertWebEntity(com.clustercontrol.xcloud.model.ExtendedProperty entity) {
		ExtendedProperty property = new ExtendedProperty();
		property.setName(entity.getName());
		property.setValue(entity.getValue());
		return property;
	}
}
