/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class JmxMasterInfoResponse {

	private String id;
	private String objectName;
	private String attributeName;
	private String keys;
	private String name;
	@RestPartiallyTransrateTarget
	private String nameTransrate;
	private String measure;
	@RestPartiallyTransrateTarget
	private String measureTransrate;

	public JmxMasterInfoResponse() {
	}

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
	public String getNameTransrate() {
		return nameTransrate;
	}
	public void setNameTransrate(String nameTransrate) {
		this.nameTransrate = nameTransrate;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public String getMeasureTransrate() {
		return measureTransrate;
	}
	public void setMeasureTransrate(String measureTransrate) {
		this.measureTransrate = measureTransrate;
	}

	@Override
	public String toString() {
		return "JmxMasterInfoResponse [id=" + id + ", objectName=" + objectName + ", attributeName=" + attributeName
				+ ", keys=" + keys + ", name=" + name + ", nameTransrate=" + nameTransrate + ", measure=" + measure
				+ ", measureTransrate=" + measureTransrate + "]";
	}

}
