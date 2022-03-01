/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddStatusNotifyRequest extends AbstractAddNotifyRequest {

	private StatusNotifyDetailInfoRequest notifyStatusInfo;

	public AddStatusNotifyRequest() {
	}

	public StatusNotifyDetailInfoRequest getNotifyStatusInfo() {
		return notifyStatusInfo;
	}

	public void setNotifyStatusInfo(StatusNotifyDetailInfoRequest notifyStatusInfo) {
		this.notifyStatusInfo = notifyStatusInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
