/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

public class EventUserExtensionItemInfoResponse {
	public EventUserExtensionItemInfoResponse() {
	}

	private Boolean displayEnable = null;
	private String displayName = null;
	private Boolean modifyClientEnable = null;

	public Boolean getDisplayEnable() {
		return displayEnable;
	}

	public void setDisplayEnable(Boolean displayEnable) {
		this.displayEnable = displayEnable;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Boolean getModifyClientEnable() {
		return modifyClientEnable;
	}

	public void setModifyClientEnable(Boolean modifyClientEnable) {
		this.modifyClientEnable = modifyClientEnable;
	}

	@Override
	public String toString() {
		return "EventUserExtensionItemInfoResponse [displayEnable=" + displayEnable + ", displayName=" + displayName
				+ ", modifyClientEnable=" + modifyClientEnable + "]";
	}
}
