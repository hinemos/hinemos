/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum NotifyPriorityEnum implements EnumDto<Integer> {

	CRITICAL(PriorityConstant.TYPE_CRITICAL),
	UNKNOWN(PriorityConstant.TYPE_UNKNOWN),
	WARNING(PriorityConstant.TYPE_WARNING),
	INFO(PriorityConstant.TYPE_INFO);

	private final Integer code;

	private NotifyPriorityEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
