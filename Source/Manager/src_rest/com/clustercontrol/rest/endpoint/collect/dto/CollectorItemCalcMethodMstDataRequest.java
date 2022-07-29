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

public class CollectorItemCalcMethodMstDataRequest implements RequestDto {
	private String collectMethod;
	private String platformId;
	private String subPlatformId;
	private String itemCode;
	private String calcMethod;

	public CollectorItemCalcMethodMstDataRequest() {
	}

	public String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
	}

	public String getPlatformId() {
		return this.platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getSubPlatformId() {
		return this.subPlatformId;
	}

	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public String getItemCode() {
		return this.itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getCalcMethod() {
		return this.calcMethod;
	}

	public void setCalcMethod(String calcMethod) {
		this.calcMethod = calcMethod;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}