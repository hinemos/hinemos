/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class ModifyCommandNotifyRequest extends AbstractModifyNotifyRequest {
	private CommandNotifyDetailInfoRequest notifyCommandInfo;

	public ModifyCommandNotifyRequest() {
	}

	public CommandNotifyDetailInfoRequest getNotifyCommandInfo() {
		return notifyCommandInfo;
	}

	public void setNotifyCommandInfo(CommandNotifyDetailInfoRequest notifyCommandInfo) {
		this.notifyCommandInfo = notifyCommandInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
