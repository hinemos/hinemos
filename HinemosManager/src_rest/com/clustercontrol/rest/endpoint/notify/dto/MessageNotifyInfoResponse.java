/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class MessageNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private MessageNotifyDetailInfoResponse notifyMessageInfo;

	public MessageNotifyInfoResponse() {
	}

	public MessageNotifyDetailInfoResponse getNotifyMessageInfo() {
		return notifyMessageInfo;
	}

	public void setNotifyMessageInfo(MessageNotifyDetailInfoResponse notifyMessageInfo) {
		this.notifyMessageInfo = notifyMessageInfo;
	}
}
