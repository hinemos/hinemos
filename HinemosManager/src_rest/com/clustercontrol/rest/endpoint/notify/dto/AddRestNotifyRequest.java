/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddRestNotifyRequest extends AbstractAddNotifyRequest {
	private RestNotifyDetailInfoRequest notifyRestInfo;

	public AddRestNotifyRequest() {
	}

	public RestNotifyDetailInfoRequest getNotifyRestInfo() {
		return notifyRestInfo;
	}

	public void setNotifyRestInfo(RestNotifyDetailInfoRequest notifyRestInfo) {
		this.notifyRestInfo = notifyRestInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if(notifyRestInfo!=null){
			notifyRestInfo.correlationCheck();
		}
	}
}
