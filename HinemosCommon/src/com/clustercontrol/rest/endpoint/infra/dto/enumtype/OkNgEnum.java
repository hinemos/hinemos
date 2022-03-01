/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum OkNgEnum implements EnumDto<Integer> {

	NG(0), OK(1), SKIP(2);

	private final Integer code;

	private OkNgEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
