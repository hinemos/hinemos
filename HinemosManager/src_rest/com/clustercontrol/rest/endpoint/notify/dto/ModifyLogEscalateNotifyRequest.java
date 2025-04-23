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

public class ModifyLogEscalateNotifyRequest extends AbstractModifyNotifyRequest {
	private LogEscalateNotifyDetailInfoRequest notifyLogEscalateInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public ModifyLogEscalateNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.LOG_ESCALATE;
		super.setNotifyType(type);
	}

	public LogEscalateNotifyDetailInfoRequest getNotifyLogEscalateInfo() {
		return notifyLogEscalateInfo;
	}

	public void setNotifyLogEscalateInfo(LogEscalateNotifyDetailInfoRequest notifyLogEscalateInfo) {
		this.notifyLogEscalateInfo = notifyLogEscalateInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
