/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class CollectorItemCodeMstDataResponse {
	private String itemCode;
	private String categoryCode;
	private String parentItemCode;
	 private String itemName;
	@RestPartiallyTransrateTarget
	private String itemNameTransrate;
	private String measure;
	@RestPartiallyTransrateTarget
	private String measureTransrate;
	private Boolean deviceSupport;
	private String deviceType;
	private Boolean graphRange;

	public CollectorItemCodeMstDataResponse() {
	}

	public String getItemCode() {
		return this.itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getParentItemCode() {
		return this.parentItemCode;
	}

	public void setParentItemCode(String parentItemCode) {
		this.parentItemCode = parentItemCode;
	}

	public String getItemName() {
		return this.itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemNameTransrate() {
		return itemNameTransrate;
	}

	public void setItemNameTransrate(String itemNameTransrate) {
		this.itemNameTransrate = itemNameTransrate;
	}

	public String getMeasure() {
		return this.measure;
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

	public Boolean isDeviceSupport() {
		return this.deviceSupport;
	}

	public void setDeviceSupport(Boolean deviceSupport) {
		this.deviceSupport = deviceSupport;
	}

	public String getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public Boolean isGraphRange() {
		return this.graphRange;
	}

	public void setGraphRange(Boolean graphRange) {
		this.graphRange = graphRange;
	}
}
