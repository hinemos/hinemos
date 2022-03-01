/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class MailNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private MailNotifyDetailInfoResponse notifyMailInfo;

	public MailNotifyInfoResponse() {
	}

	public MailNotifyDetailInfoResponse getNotifyMailInfo() {
		return notifyMailInfo;
	}

	public void setNotifyMailInfo(MailNotifyDetailInfoResponse notifyMailInfo) {
		this.notifyMailInfo = notifyMailInfo;
	}
}
