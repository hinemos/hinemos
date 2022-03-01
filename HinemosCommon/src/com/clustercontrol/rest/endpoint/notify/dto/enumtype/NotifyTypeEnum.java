/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum NotifyTypeEnum implements EnumDto<Integer> {

	STATUS(NotifyTypeConstant.TYPE_STATUS),
	EVENT(NotifyTypeConstant.TYPE_EVENT),
	MAIL(NotifyTypeConstant.TYPE_MAIL),
	JOB(NotifyTypeConstant.TYPE_JOB),
	LOG_ESCALATE(NotifyTypeConstant.TYPE_LOG_ESCALATE),
	COMMAND(NotifyTypeConstant.TYPE_COMMAND),
	INFRA(NotifyTypeConstant.TYPE_INFRA),
	REST(NotifyTypeConstant.TYPE_REST),
	CLOUD(NotifyTypeConstant.TYPE_CLOUD),
	MESSAGE(NotifyTypeConstant.TYPE_MESSAGE);

	private final Integer code;

	private NotifyTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
