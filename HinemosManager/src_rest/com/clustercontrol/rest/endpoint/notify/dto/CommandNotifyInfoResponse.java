/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class CommandNotifyInfoResponse extends AbstractNotifyInfoResponse {
	private CommandNotifyDetailInfoResponse notifyCommandInfo;

	public CommandNotifyInfoResponse() {
	}

	public CommandNotifyDetailInfoResponse getNotifyCommandInfo() {
		return notifyCommandInfo;
	}

	public void setNotifyCommandInfo(CommandNotifyDetailInfoResponse notifyCommandInfo) {
		this.notifyCommandInfo = notifyCommandInfo;
	}
}
