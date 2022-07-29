/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddInfraNotifyRequest extends AbstractAddNotifyRequest {

	private InfraNotifyDetailInfoRequest notifyInfraInfo;

	public AddInfraNotifyRequest() {
	}

	public InfraNotifyDetailInfoRequest getNotifyInfraInfo() {
		return notifyInfraInfo;
	}

	public void setNotifyInfraInfo(InfraNotifyDetailInfoRequest notifyInfraInfo) {
		this.notifyInfraInfo = notifyInfraInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
