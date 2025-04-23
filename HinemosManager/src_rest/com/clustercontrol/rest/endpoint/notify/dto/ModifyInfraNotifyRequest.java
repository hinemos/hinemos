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

public class ModifyInfraNotifyRequest extends AbstractModifyNotifyRequest {
	private InfraNotifyDetailInfoRequest notifyInfraInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public ModifyInfraNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.INFRA;
		super.setNotifyType(type);
	}

	public InfraNotifyDetailInfoRequest getNotifyInfraInfo() {
		return notifyInfraInfo;
	}

	public void setNotifyInfraInfo(InfraNotifyDetailInfoRequest notifyInfraInfo) {
		this.notifyInfraInfo = notifyInfraInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
