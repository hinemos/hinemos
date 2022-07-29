/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddCollectPlatformMasterRequest implements RequestDto {

	@RestItemName(value = MessageConstant.PLATFORM_ID)
	@RestValidateString(notNull=true, minLen=1, maxLen=64)
	private String platformId;
	@RestItemName(value = MessageConstant.PLATFORM_NAME)
	@RestValidateString(notNull=true, minLen=1, maxLen=256)
	private String platformName;
	@RestItemName(value = MessageConstant.ORDER_NO)
	@RestValidateInteger(notNull=true)
	private Short orderNo;

	public AddCollectPlatformMasterRequest() {
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public Short getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Short orderNo) {
		this.orderNo = orderNo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
