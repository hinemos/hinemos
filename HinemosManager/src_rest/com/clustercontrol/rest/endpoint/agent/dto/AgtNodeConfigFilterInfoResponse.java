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

@RestBeanConvertAssertion(from = NodeConfigFilterInfo.class)
public class AgtNodeConfigFilterInfoResponse {

	// ---- from NodeConfigFilterInfo
	private String nodeConfigSettingItemName;
	private Boolean exists;
	private List<AgtNodeConfigFilterItemInfoResponse> itemList;
	private NodeConfigSettingItem nodeConfigSettingItem;

	public AgtNodeConfigFilterInfoResponse() {
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

	public List<AgtNodeConfigFilterItemInfoResponse> getItemList() {
		return itemList;
	}

	public void setItemList(List<AgtNodeConfigFilterItemInfoResponse> itemList) {
		this.itemList = itemList;
	}

	public NodeConfigSettingItem getNodeConfigSettingItem() {
		return nodeConfigSettingItem;
	}

	public void setNodeConfigSettingItem(NodeConfigSettingItem nodeConfigSettingItem) {
		this.nodeConfigSettingItem = nodeConfigSettingItem;
	}

}
