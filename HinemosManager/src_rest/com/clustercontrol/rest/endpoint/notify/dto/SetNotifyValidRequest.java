/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class SetNotifyValidRequest implements RequestDto {

	@RestItemName(value = MessageConstant.NOTIFY_ID)
	private List<String> notifyIds;

	private Boolean validFlg;

	public SetNotifyValidRequest() {
	}

	public List<String> getNotifyIds() {
		return notifyIds;
	}

	public void setNotifyIds(List<String> notifyIds) {
		this.notifyIds = notifyIds;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
