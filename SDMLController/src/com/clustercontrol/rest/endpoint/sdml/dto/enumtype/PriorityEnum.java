/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum PriorityEnum implements EnumDto<Integer> {

	CRITICAL(0),
	UNKNOWN(1),
	WARNING(2),
	INFO(3),
	NONE(4);

	private final Integer code;

	private PriorityEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
