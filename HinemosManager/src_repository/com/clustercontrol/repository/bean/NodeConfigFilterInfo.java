/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * Hinemosの構成情報検索条件を格納するクラスです。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用します。
 *
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeConfigFilterInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = -8083826422320617790L;

	// 構成情報名
	private String nodeConfigSettingItemName = "";

	// Exists/Not Exists条件 (true=Exists)
	private Boolean exists = true;

	// 構成情報項目
	private List<NodeConfigFilterItemInfo> itemList = new ArrayList<>();

	// 構成情報
	private NodeConfigSettingItem nodeConfigSettingItem = null;

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

	public List<NodeConfigFilterItemInfo> getItemList() {
		return itemList;
	}
	public void setItemList(List<NodeConfigFilterItemInfo> itemList) {
		this.itemList = itemList;
	}

	@XmlTransient
	public NodeConfigSettingItem getNodeConfigSettingItem() {
		try {
			return NodeConfigSettingItem.valueOf(this.nodeConfigSettingItemName);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public NodeConfigFilterInfo clone() {
		try {
			NodeConfigFilterInfo cloneInfo = (NodeConfigFilterInfo)super.clone();
			cloneInfo.nodeConfigSettingItemName = this.nodeConfigSettingItemName;
			cloneInfo.exists = this.exists;
			cloneInfo.nodeConfigSettingItem = this.nodeConfigSettingItem;
			List<NodeConfigFilterItemInfo> itemList = new ArrayList<>();
			for (NodeConfigFilterItemInfo itemInfo : this.getItemList()) {
				itemList.add(itemInfo.clone());
			}
			cloneInfo.setItemList(itemList);
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
}
