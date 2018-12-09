/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
