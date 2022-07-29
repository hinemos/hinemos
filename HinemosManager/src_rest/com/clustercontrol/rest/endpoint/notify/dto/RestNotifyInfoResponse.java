/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class RestNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private RestNotifyDetailInfoResponse notifyRestInfo;

	public RestNotifyInfoResponse() {
	}

	public RestNotifyDetailInfoResponse getNotifyRestInfo() {
		return notifyRestInfo;
	}

	public void setNotifyRestInfo(RestNotifyDetailInfoResponse notifyRestInfo) {
		this.notifyRestInfo = notifyRestInfo;
	}
}
