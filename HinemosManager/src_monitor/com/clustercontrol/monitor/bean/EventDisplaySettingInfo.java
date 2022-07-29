/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * イベント表示の設定情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventDisplaySettingInfo implements Serializable {
	
	private static final long serialVersionUID = -7938631637638210611L;
	
	private Map<Integer, EventUserExtensionItemInfo> userItemInfoMap;
	private EventNoDisplayInfo eventNoInfo;
	
	public Map<Integer, EventUserExtensionItemInfo> getUserItemInfoMap() {
		return userItemInfoMap;
	}
	public void setUserItemInfoMap(Map<Integer, EventUserExtensionItemInfo> userItemInfoMap) {
		this.userItemInfoMap = userItemInfoMap;
	}
	public EventNoDisplayInfo getEventNoInfo() {
		return eventNoInfo;
	}
	public void setEventNoInfo(EventNoDisplayInfo eventNoInfo) {
		this.eventNoInfo = eventNoInfo;
	}
}
