/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum EventNormalStateEnum implements EnumDto<Integer> {

	UNCONFIRMED(ConfirmConstant.TYPE_UNCONFIRMED),
	CONFIRMED(ConfirmConstant.TYPE_CONFIRMED),
	CONFIRMING(ConfirmConstant.TYPE_CONFIRMING);

	private final Integer code;

	private EventNormalStateEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
