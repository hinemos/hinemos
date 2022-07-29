/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ModuleTypeEnum implements EnumDto<Integer> {

	COMMAND(0), FILETRANSFER(1), REFERMANAGEMENT(2);

	private final Integer code;

	private ModuleTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
