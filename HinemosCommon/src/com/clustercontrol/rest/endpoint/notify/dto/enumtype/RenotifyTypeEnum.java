/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.notify.bean.RenotifyTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RenotifyTypeEnum implements EnumDto<Integer> {

	ALWAYS_NOTIFY(RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY),
	PERIOD(RenotifyTypeConstant.TYPE_PERIOD),
	NO_NOTIFY(RenotifyTypeConstant.TYPE_NO_NOTIFY);

	private final Integer code;

	private RenotifyTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
