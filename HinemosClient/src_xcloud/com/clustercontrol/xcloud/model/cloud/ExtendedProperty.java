/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import org.openapitools.client.model.ExtendedPropertyResponse;

import com.clustercontrol.xcloud.model.base.Element;

public class ExtendedProperty extends Element implements IExtendedProperty{
	private String name = null;
	private String value = null;

	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		internalSetProperty(p.name, name, ()->this.name, (s)->this.name=s);
	}

	@Override
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		internalSetProperty(p.value, value, ()->this.value, (s)->this.value=s);
	}
	
	protected void update(ExtendedPropertyResponse source) {
		setName(source.getName());
		setValue(source.getValue());
	}
	
	public static ExtendedProperty convert(ExtendedPropertyResponse source) {
		ExtendedProperty storage = new ExtendedProperty();
		storage.update(source);
		return storage;
	}
}
