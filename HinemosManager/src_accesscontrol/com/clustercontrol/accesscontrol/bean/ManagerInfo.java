/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.bean;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosマネージャの情報を格納するクラス。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用する。
 *
 */
@XmlType(namespace = "http://access.ws.clustercontrol.com")
public class ManagerInfo implements Serializable {
	private static final long serialVersionUID = 4644694553453455893L;

	private int timeZoneOffset;
	private Set<String> options;

	public ManagerInfo(){}

	public ManagerInfo(int timeZoneOffset, Set<String> options){
		this.timeZoneOffset = timeZoneOffset;
		this.options = options;
	}

	public int getTimeZoneOffset() {
		return timeZoneOffset;
	}
	public void setTimeZoneOffset(int timeZoneOffset) {
		this.timeZoneOffset = timeZoneOffset;
	}
	public Set<String> getOptions() {
		return options;
	}
	public void setOptions(Set<String> options) {
		this.options = options;
	}
}