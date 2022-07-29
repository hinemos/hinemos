/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * イベント番号の表示設定情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventNoDisplayInfo implements Serializable {
	
	private static final long serialVersionUID = -688724514461420582L;
	
	private Boolean displayEnable = null;
	private Boolean exportEnable = null;
	public Boolean getDisplayEnable() {
		return displayEnable;
	}
	public void setDisplayEnable(Boolean displayEnable) {
		this.displayEnable = displayEnable;
	}
	public Boolean getExportEnable() {
		return exportEnable;
	}
	public void setExportEnable(Boolean exportEnable) {
		this.exportEnable = exportEnable;
	}
}
