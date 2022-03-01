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

public class AddCollectSubPlatformMasterRequest implements RequestDto {

	@RestItemName(value = MessageConstant.SUB_PLATFORM_ID)
	@RestValidateString(notNull=true, minLen=1, maxLen=64)
	private String subPlatformId;
	@RestItemName(value = MessageConstant.SUB_PLATFORM_NAME)
	@RestValidateString(notNull=true, minLen=1, maxLen=256)
	private String subPlatformName;
	@RestItemName(value = MessageConstant.TYPE)
	@RestValidateString(notNull=true, minLen=1, maxLen=256)
	private String type;
	@RestItemName(value = MessageConstant.ORDER_NO)
	@RestValidateInteger(notNull=true)
	private Integer orderNo;

	public AddCollectSubPlatformMasterRequest() {
	}

	public String getSubPlatformId() {
		return this.subPlatformId;
	}

	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public String getSubPlatformName() {
		return this.subPlatformName;
	}

	public void setSubPlatformName(String subPlatformName) {
		this.subPlatformName = subPlatformName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
