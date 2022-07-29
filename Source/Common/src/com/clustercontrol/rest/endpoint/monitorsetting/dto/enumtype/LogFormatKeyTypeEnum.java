/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum LogFormatKeyTypeEnum implements EnumDto<Integer> {

	parsing(0), fixed(1);

	private final Integer code;

	private LogFormatKeyTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
