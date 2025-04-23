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

public class ModifyStatusNotifyRequest extends AbstractModifyNotifyRequest {
	private StatusNotifyDetailInfoRequest notifyStatusInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public ModifyStatusNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.STATUS;
		super.setNotifyType(type);
	}

	public StatusNotifyDetailInfoRequest getNotifyStatusInfo() {
		return notifyStatusInfo;
	}

	public void setNotifyStatusInfo(StatusNotifyDetailInfoRequest notifyStatusInfo) {
		this.notifyStatusInfo = notifyStatusInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
