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

public class AddCloudNotifyRequest extends AbstractAddNotifyRequest {

	private CloudNotifyDetailInfoRequest notifyCloudInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public AddCloudNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.CLOUD;
		super.setNotifyType(type);
	}

	public CloudNotifyDetailInfoRequest getNotifyCloudInfo() {
		return notifyCloudInfo;
	}

	public void setNotifyCloudeInfo(CloudNotifyDetailInfoRequest notifyCloudInfo) {
		this.notifyCloudInfo = notifyCloudInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
