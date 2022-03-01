/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddLogEscalateNotifyRequest extends AbstractAddNotifyRequest {

	private LogEscalateNotifyDetailInfoRequest notifyLogEscalateInfo;

	public AddLogEscalateNotifyRequest() {
	}

	public LogEscalateNotifyDetailInfoRequest getNotifyLogEscalateInfo() {
		return notifyLogEscalateInfo;
	}

	public void setNotifyLogEscalateInfo(LogEscalateNotifyDetailInfoRequest notifyLogEscalateInfo) {
		this.notifyLogEscalateInfo = notifyLogEscalateInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
