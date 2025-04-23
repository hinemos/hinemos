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

public class ModifyEventNotifyRequest extends AbstractModifyNotifyRequest {
	private EventNotifyDetailInfoRequest notifyEventInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public ModifyEventNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.EVENT;
		super.setNotifyType(type);
	}

	public EventNotifyDetailInfoRequest getNotifyEventInfo() {
		return notifyEventInfo;
	}

	public void setNotifyEventInfo(EventNotifyDetailInfoRequest notifyEventInfo) {
		this.notifyEventInfo = notifyEventInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
