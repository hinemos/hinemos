/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum AccessMethodTypeEnum implements EnumDto<Integer>{
	ACCESS_METHOD_TYPE_SSH(0),ACCESS_METHOD_TYPE_WINRM(1);

	private final Integer code;

	private AccessMethodTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}

