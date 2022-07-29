/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddCloudNotifyRequest extends AbstractAddNotifyRequest {

	private CloudNotifyDetailInfoRequest notifyCloudInfo;

	public AddCloudNotifyRequest() {
	}

	public CloudNotifyDetailInfoRequest getNotifyCloudInfo() {
		return notifyCloudInfo;
	}

	public void setNotifyCloudeInfo(CloudNotifyDetailInfoRequest notifyCloudInfo) {
		this.notifyCloudInfo = notifyCloudInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
