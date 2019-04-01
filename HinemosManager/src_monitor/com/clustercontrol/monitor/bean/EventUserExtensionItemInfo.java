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
 * ユーザ拡張イベント項目の設定情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventUserExtensionItemInfo implements Serializable {
	
	private static final long serialVersionUID = -5295170871936328522L;
	
	private Boolean displayEnable = null;
	private Boolean exportEnable = null;
	private String displayName = null;
	private String registInitValue = null;
	private Boolean modifyClientEnable = null;
	private Boolean modifyRequired = null;
	private String modifyValidation = null;
	private String modifyFormat = null;
	
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
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getRegistInitValue() {
		return registInitValue;
	}
	public void setRegistInitValue(String registInitValue) {
		this.registInitValue = registInitValue;
	}
	public Boolean getModifyClientEnable() {
		return modifyClientEnable;
	}
	public void setModifyClientEnable(Boolean modifyClientEnable) {
		this.modifyClientEnable = modifyClientEnable;
	}
	public Boolean getModifyRequired() {
		return modifyRequired;
	}
	public void setModifyRequired(Boolean modifyRequired) {
		this.modifyRequired = modifyRequired;
	}
	public String getModifyValidation() {
		return modifyValidation;
	}
	public void setModifyValidation(String modifyValidation) {
		this.modifyValidation = modifyValidation;
	}
	public String getModifyFormat() {
		return modifyFormat;
	}
	public void setModifyFormat(String modifyFormat) {
		this.modifyFormat = modifyFormat;
	}
}
