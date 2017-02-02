/*

Copyright (C) since 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
