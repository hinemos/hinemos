/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class JobNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private JobNotifyDetailInfoResponse notifyJobInfo;

	public JobNotifyInfoResponse() {
	}

	public JobNotifyDetailInfoResponse getNotifyJobInfo() {
		return notifyJobInfo;
	}

	public void setNotifyJobInfo(JobNotifyDetailInfoResponse notifyJobInfo) {
		this.notifyJobInfo = notifyJobInfo;
	}
}
