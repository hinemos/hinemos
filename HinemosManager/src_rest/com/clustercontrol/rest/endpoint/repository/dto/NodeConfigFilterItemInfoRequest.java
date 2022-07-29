/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigFilterItemEnum;

public class NodeConfigFilterItemInfoRequest implements RequestDto {

	/** 項目名 */
	@RestBeanConvertEnum
	private NodeConfigFilterItemEnum itemName;

	/** 比較演算子 */
	private String method;

	/** 値 */
	private String itemStringValue;
	private Long itemLongValue;
	private Integer itemIntegerValue;

	public NodeConfigFilterItemInfoRequest() {
	}

	public NodeConfigFilterItemEnum getItemName() {
		return itemName;
	}

	public void setItemName(NodeConfigFilterItemEnum itemName) {
		this.itemName = itemName;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}


	public String getItemStringValue() {
		return itemStringValue;
	}

	public void setItemStringValue(String itemStringValue) {
		this.itemStringValue = itemStringValue;
	}

	public Long getItemLongValue() {
		return itemLongValue;
	}

	public void setItemLongValue(Long itemLongValue) {
		this.itemLongValue = itemLongValue;
	}

	public Integer getItemIntegerValue() {
		return itemIntegerValue;
	}

	public void setItemIntegerValue(Integer itemIntegerValue) {
		this.itemIntegerValue = itemIntegerValue;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
