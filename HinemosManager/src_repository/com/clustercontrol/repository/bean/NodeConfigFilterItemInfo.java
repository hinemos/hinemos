/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosの構成情報検索条件の詳細情報を格納するクラスです。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用します。
 *
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeConfigFilterItemInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = -8083826422320617790L;

	// 項目名
	private String itemName = "";
	// 比較演算子
	private String method = "";
	// 値
	private String itemStringValue;
	private Long itemLongValue;
	private Integer itemIntegerValue = null;

	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
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
	public Object getItemValue() {
		if (getItem().dataType() == NodeConfigFilterDataType.INTEGER
				|| getItem().dataType() == NodeConfigFilterDataType.INTEGER_ONLYEQUAL) {
			return this.itemIntegerValue;
		} else if (getItem().dataType() == NodeConfigFilterDataType.STRING
				|| getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL
				|| getItem().dataType() == NodeConfigFilterDataType.STRING_VERSION) {
			return this.itemStringValue;
		} else if (getItem().dataType() == NodeConfigFilterDataType.DATETIME) {
			return this.itemLongValue;
		} else {
			return null;
		}
	}
	@XmlTransient
	public NodeConfigFilterItem getItem() {
		return NodeConfigFilterItem.valueOf(this.itemName);
	}
	@XmlTransient
	public NodeConfigFilterComparisonMethod getMethodType() {
		return NodeConfigFilterComparisonMethod.symbolToType(this.method);
	}
	@Override
	public NodeConfigFilterItemInfo clone() {
		try {
			NodeConfigFilterItemInfo cloneInfo = (NodeConfigFilterItemInfo)super.clone();
			cloneInfo.itemName = this.itemName;
			cloneInfo.method = this.method;
			cloneInfo.itemStringValue = this.itemStringValue;
			cloneInfo.itemLongValue = this.itemLongValue;
			cloneInfo.itemIntegerValue = this.itemIntegerValue;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
}
