/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ConvertFlagEnum implements EnumDto<Integer> {

	NONE(0), DELTA(1);

	private final Integer code;

	private ConvertFlagEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
