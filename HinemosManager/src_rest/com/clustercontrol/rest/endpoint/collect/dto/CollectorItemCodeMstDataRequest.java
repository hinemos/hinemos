/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class CollectorItemCodeMstDataRequest implements RequestDto {
	private String itemCode;
	private String categoryCode;
	private String parentItemCode;
	private String itemName;
	private String measure;
	private Boolean deviceSupport;
	private String deviceType;
	private Boolean graphRange;

	public CollectorItemCodeMstDataRequest() {
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

	public String getMeasure() {
		return this.measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
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

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}