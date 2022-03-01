/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import java.util.Map;

public class EventDisplaySettingInfoResponse {
	public EventDisplaySettingInfoResponse() {
	}

	private Map<Integer, EventUserExtensionItemInfoResponse> userItemInfoMap;
	private EventNoDisplayInfoResponse eventNoInfo;

	public Map<Integer, EventUserExtensionItemInfoResponse> getUserItemInfoMap() {
		return userItemInfoMap;
	}

	public void setUserItemInfoMap(Map<Integer, EventUserExtensionItemInfoResponse> userItemInfoMap) {
		this.userItemInfoMap = userItemInfoMap;
	}

	public EventNoDisplayInfoResponse getEventNoInfo() {
		return eventNoInfo;
	}

	public void setEventNoInfo(EventNoDisplayInfoResponse eventNoInfo) {
		this.eventNoInfo = eventNoInfo;
	}

	@Override
	public String toString() {
		return "EventDisplaySettingInfoResponse [userItemInfoMap=" + userItemInfoMap + ", eventNoInfo=" + eventNoInfo
				+ "]";
	}
}