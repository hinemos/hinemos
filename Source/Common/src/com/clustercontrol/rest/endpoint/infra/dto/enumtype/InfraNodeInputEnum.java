/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum InfraNodeInputEnum implements EnumDto<Integer> {

	NODE_PARAM(0), INFRA_PARAM(1), DIALOG(2);

	private final Integer code;

	private InfraNodeInputEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
