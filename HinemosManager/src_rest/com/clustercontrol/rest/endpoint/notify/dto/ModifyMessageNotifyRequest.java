/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;

public class ModifyMessageNotifyRequest extends AbstractModifyNotifyRequest {
	@RestValidateObject(notNull = true)
	private MessageNotifyDetailInfoRequest notifyMessageInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public ModifyMessageNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.MESSAGE;
		super.setNotifyType(type);
	}

	public MessageNotifyDetailInfoRequest getNotifyMessageInfo() {
		return notifyMessageInfo;
	}

	public void setNotifyMessageInfo(MessageNotifyDetailInfoRequest notifyMessageInfo) {
		this.notifyMessageInfo = notifyMessageInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		this.notifyMessageInfo.correlationCheck();
	}
}
