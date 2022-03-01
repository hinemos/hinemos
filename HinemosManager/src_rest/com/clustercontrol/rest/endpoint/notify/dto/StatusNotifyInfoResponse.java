/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class StatusNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private StatusNotifyDetailInfoResponse notifyStatusInfo;

	public StatusNotifyInfoResponse() {
	}

	public StatusNotifyDetailInfoResponse getNotifyStatusInfo() {
		return notifyStatusInfo;
	}

	public void setNotifyStatusInfo(StatusNotifyDetailInfoResponse notifyStatusInfo) {
		this.notifyStatusInfo = notifyStatusInfo;
	}
}
