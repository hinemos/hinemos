/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;

public class ModifyCommandNotifyRequest extends AbstractModifyNotifyRequest {
	private CommandNotifyDetailInfoRequest notifyCommandInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public ModifyCommandNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.COMMAND;
		super.setNotifyType(type);
	}

	public CommandNotifyDetailInfoRequest getNotifyCommandInfo() {
		return notifyCommandInfo;
	}

	public void setNotifyCommandInfo(CommandNotifyDetailInfoRequest notifyCommandInfo) {
		this.notifyCommandInfo = notifyCommandInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
