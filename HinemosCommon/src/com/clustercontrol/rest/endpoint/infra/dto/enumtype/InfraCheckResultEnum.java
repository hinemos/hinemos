/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum InfraCheckResultEnum implements EnumDto<Integer> {
	NG(0), OK(1);

	private final Integer code;

	private InfraCheckResultEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
