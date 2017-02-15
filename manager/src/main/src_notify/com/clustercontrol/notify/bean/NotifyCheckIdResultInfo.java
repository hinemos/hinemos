/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.bean;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

/**
 * 通知情報を保持するクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyCheckIdResultInfo implements Serializable
{
	private static final long serialVersionUID = 7640475055588453464L;

	private String notifyId;
	private ArrayList<String> notifyGroupIdList = new ArrayList<String>();

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	public String getNotifyId() {
		return this.notifyId;
	}

	public void setNotifyGroupIdList(ArrayList<String> notiyfGroupIdList) {
		this.notifyGroupIdList = notiyfGroupIdList;
	}

	public ArrayList<String> getNotifyGroupIdList() {
		return this.notifyGroupIdList;
	}

}
