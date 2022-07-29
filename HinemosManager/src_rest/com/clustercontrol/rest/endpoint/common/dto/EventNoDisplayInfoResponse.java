/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

public class EventNoDisplayInfoResponse {
	public EventNoDisplayInfoResponse() {
	}

	private Boolean displayEnable = null;

	public Boolean getDisplayEnable() {
		return displayEnable;
	}

	public void setDisplayEnable(Boolean displayEnable) {
		this.displayEnable = displayEnable;
	}

	@Override
	public String toString() {
		return "EventNoDisplayInfoResponse [displayEnable=" + displayEnable + "]";
	}
}
