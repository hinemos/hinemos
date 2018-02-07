/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * このクラスはDeviceSearchの差分表示用クラスです。
 *
 * @since 5.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class DeviceSearchMessageInfo implements Serializable
{

	private static final long serialVersionUID = 5047737569380336435L;
	private String itemName;
	private String lastVal;
	private String thisVal;

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemName() {
		return itemName;
	}

	public String getLastVal() {
		return lastVal;
	}

	public void setLastVal(String lastVal) {
		this.lastVal = lastVal;
	}

	public String getThisVal() {
		return thisVal;
	}

	public void setThisVal(String thisVal) {
		this.thisVal = thisVal;
	}

}
