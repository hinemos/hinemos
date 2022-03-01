/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum SnmptrapVersionEnum implements EnumDto<Integer> {

	V1(0), V2C_V3(1);

	private final Integer code;

	private SnmptrapVersionEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
