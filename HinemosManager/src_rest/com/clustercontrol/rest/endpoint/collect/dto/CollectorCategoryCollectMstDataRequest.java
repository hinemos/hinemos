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

public class CollectorCategoryCollectMstDataRequest implements RequestDto {
	private String platformId;
	private String subPlatformId;
	private String categoryCode;
	private String collectMethod;

	public CollectorCategoryCollectMstDataRequest() {
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

	public String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}