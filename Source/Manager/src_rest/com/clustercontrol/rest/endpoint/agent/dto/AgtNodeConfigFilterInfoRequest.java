/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.repository.bean.NodeConfigFilterInfo;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = NodeConfigFilterInfo.class)
public class AgtNodeConfigFilterInfoRequest extends AgentRequestDto {

	// ---- from NodeConfigFilterInfo
	private String nodeConfigSettingItemName;
	private Boolean exists;
	private List<AgtNodeConfigFilterItemInfoRequest> itemList;
	private NodeConfigSettingItem nodeConfigSettingItem;

	public AgtNodeConfigFilterInfoRequest() {
	}

	// ---- accessors

	public String getNodeConfigSettingItemName() {
		return nodeConfigSettingItemName;
	}

	public void setNodeConfigSettingItemName(String nodeConfigSettingItemName) {
		this.nodeConfigSettingItemName = nodeConfigSettingItemName;
	}

	public Boolean getExists() {
		return exists;
	}

	public void setExists(Boolean exists) {
		this.exists = exists;
	}

	public List<AgtNodeConfigFilterItemInfoRequest> getItemList() {
		return itemList;
	}

	public void setItemList(List<AgtNodeConfigFilterItemInfoRequest> itemList) {
		this.itemList = itemList;
	}

	public NodeConfigSettingItem getNodeConfigSettingItem() {
		return nodeConfigSettingItem;
	}

	public void setNodeConfigSettingItem(NodeConfigSettingItem nodeConfigSettingItem) {
		this.nodeConfigSettingItem = nodeConfigSettingItem;
	}

}
