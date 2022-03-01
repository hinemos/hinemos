/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class JmxMasterInfoRequest implements RequestDto {

	public JmxMasterInfoRequest() {

	}

	private String id;
	private String objectName;
	private String attributeName;
	private String keys;
	private String name;
	private String measure;

	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getObjectName() {
		return objectName;
	}


	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}


	public String getAttributeName() {
		return attributeName;
	}


	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}


	public String getKeys() {
		return keys;
	}


	public void setKeys(String keys) {
		this.keys = keys;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getMeasure() {
		return measure;
	}


	public void setMeasure(String measure) {
		this.measure = measure;
	}


	@Override
	public String toString() {
		return "JmxMasterInfoRequest [id=" + id + ", objectName=" + objectName + ", attributeName=" + attributeName
				+ ", keys=" + keys + ", name=" + name + ", measure=" + measure + "]";
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
