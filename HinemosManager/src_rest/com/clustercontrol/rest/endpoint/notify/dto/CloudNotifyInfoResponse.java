/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class CloudNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private CloudNotifyDetailInfoResponse notifyCloudInfo;

	public CloudNotifyInfoResponse() {
	}

	public CloudNotifyDetailInfoResponse getNotifyCloudInfo() {
		return notifyCloudInfo;
	}

	public void setNotifyCloudInfo(CloudNotifyDetailInfoResponse notifyCloudInfo) {
		this.notifyCloudInfo = notifyCloudInfo;
	}
}
