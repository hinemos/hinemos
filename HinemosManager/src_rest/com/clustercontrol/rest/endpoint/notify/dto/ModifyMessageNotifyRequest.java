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

public class ModifyMessageNotifyRequest extends AbstractModifyNotifyRequest {
	@RestValidateObject(notNull = true)
	private MessageNotifyDetailInfoRequest notifyMessageInfo;

	public ModifyMessageNotifyRequest() {
	}

	public MessageNotifyDetailInfoRequest getNotifyMessageInfo() {
		return notifyMessageInfo;
	}

	public void setNotifyMessageInfo(MessageNotifyDetailInfoRequest notifyMessageInfo) {
		this.notifyMessageInfo = notifyMessageInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		this.notifyMessageInfo.correlationCheck();
	}
}
