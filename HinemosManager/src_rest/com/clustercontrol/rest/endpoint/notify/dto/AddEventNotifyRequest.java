/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddEventNotifyRequest extends AbstractAddNotifyRequest {

	private EventNotifyDetailInfoRequest notifyEventInfo;

	public AddEventNotifyRequest() {
	}

	public EventNotifyDetailInfoRequest getNotifyEventInfo() {
		return notifyEventInfo;
	}

	public void setNotifyEventInfo(EventNotifyDetailInfoRequest notifyEventInfo) {
		this.notifyEventInfo = notifyEventInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
