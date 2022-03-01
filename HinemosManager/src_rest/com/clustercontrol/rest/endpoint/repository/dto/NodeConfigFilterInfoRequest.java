/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigSettingItemEnum;

public class NodeConfigFilterInfoRequest implements RequestDto {

	/** 構成情報名 */
	@RestBeanConvertEnum
	private NodeConfigSettingItemEnum nodeConfigSettingItemName;

	/** Exists/Not Exists条件 (true=Exists) */
	private Boolean exists;

	/** 構成情報項目 */
	private List<NodeConfigFilterItemInfoRequest> itemList = new ArrayList<>();

	public NodeConfigFilterInfoRequest() {
	}

	public NodeConfigSettingItemEnum getNodeConfigSettingItemName() {
		return nodeConfigSettingItemName;
	}

	public void setNodeConfigSettingItemName(NodeConfigSettingItemEnum nodeConfigSettingItemName) {
		this.nodeConfigSettingItemName = nodeConfigSettingItemName;
	}

	public Boolean getExists() {
		return exists;
	}

	public void setExists(Boolean exists) {
		this.exists = exists;
	}

	public List<NodeConfigFilterItemInfoRequest> getItemList() {
		return itemList;
	}

	public void setItemList(List<NodeConfigFilterItemInfoRequest> itemList) {
		this.itemList = itemList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
