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
public class EventCustomCommandInfoData implements Serializable {

	private static final long serialVersionUID = -5223618768233645197L;

	private Map<Integer, EventCustomCommandInfo> evemtCustomCommandMap;

	public Map<Integer, EventCustomCommandInfo> getEvemtCustomCommandMap() {
		return evemtCustomCommandMap;
	}

	public void setEvemtCustomCommandMap(Map<Integer, EventCustomCommandInfo> evemtCustomCommandMap) {
		this.evemtCustomCommandMap = evemtCustomCommandMap;
	}
}
