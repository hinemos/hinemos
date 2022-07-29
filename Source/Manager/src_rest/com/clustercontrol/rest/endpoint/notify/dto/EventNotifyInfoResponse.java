/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class EventNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private EventNotifyDetailInfoResponse notifyEventInfo;

	public EventNotifyInfoResponse() {
	}

	public EventNotifyDetailInfoResponse getNotifyEventInfo() {
		return notifyEventInfo;
	}

	public void setNotifyEventInfo(EventNotifyDetailInfoResponse notifyEventInfo) {
		this.notifyEventInfo = notifyEventInfo;
	}
}
