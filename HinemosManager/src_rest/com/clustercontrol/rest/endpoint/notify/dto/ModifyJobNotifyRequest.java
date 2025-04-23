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

public class ModifyJobNotifyRequest extends AbstractModifyNotifyRequest {
	private JobNotifyDetailInfoRequest notifyJobInfo;

	//通知種別は固定のため、コンストラクタでデータを登録する
	public ModifyJobNotifyRequest() {
		NotifyTypeEnum type = NotifyTypeEnum.JOB;
		super.setNotifyType(type);
	}

	public JobNotifyDetailInfoRequest getNotifyJobInfo() {
		return notifyJobInfo;
	}

	public void setNotifyJobInfo(JobNotifyDetailInfoRequest notifyJobInfo) {
		this.notifyJobInfo = notifyJobInfo;
	}

	//ここでは何もしない
	@Override
	public void setNotifyType(NotifyTypeEnum notifyType) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
