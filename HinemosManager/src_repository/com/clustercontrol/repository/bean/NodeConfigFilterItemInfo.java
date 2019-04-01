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
	private Object itemValue = null;

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
	public Object getItemValue() {
		return itemValue;
	}
	public void setItemValue(Object itemValue) {
		this.itemValue = itemValue;
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
			cloneInfo.itemValue = this.itemValue;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
}
