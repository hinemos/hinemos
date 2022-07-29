/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum SendMethodTypeEnum implements EnumDto<Integer>{
	SEND_METHOD_TIPE_SCP(0),SEND_METHOD_TIPE_WINRM(1);

	private final Integer code;

	private SendMethodTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}