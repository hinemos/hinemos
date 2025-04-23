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

public class AddRestNotifyRequest extends AbstractAddNotifyRequest {
	private RestNotifyDetailInfoRequest notifyRestInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public AddRestNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.REST;
		super.setNotifyType(type);
	}

	public RestNotifyDetailInfoRequest getNotifyRestInfo() {
		return notifyRestInfo;
	}

	public void setNotifyRestInfo(RestNotifyDetailInfoRequest notifyRestInfo) {
		this.notifyRestInfo = notifyRestInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if(notifyRestInfo!=null){
			notifyRestInfo.correlationCheck();
		}
	}
}
