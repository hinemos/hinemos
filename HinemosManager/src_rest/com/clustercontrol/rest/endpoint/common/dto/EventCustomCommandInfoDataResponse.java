/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import java.util.Map;

public class EventCustomCommandInfoDataResponse {

	public EventCustomCommandInfoDataResponse() {
	}

	private Map<Integer, EventCustomCommandInfoResponse> evemtCustomCommandMap;

	public Map<Integer, EventCustomCommandInfoResponse> getEvemtCustomCommandMap() {
		return evemtCustomCommandMap;
	}

	public void setEvemtCustomCommandMap(Map<Integer, EventCustomCommandInfoResponse> evemtCustomCommandMap) {
		this.evemtCustomCommandMap = evemtCustomCommandMap;
	}

	@Override
	public String toString() {
		return "EventCustomCommandInfoDataResponse [evemtCustomCommandMap=" + evemtCustomCommandMap + "]";
	}
}
