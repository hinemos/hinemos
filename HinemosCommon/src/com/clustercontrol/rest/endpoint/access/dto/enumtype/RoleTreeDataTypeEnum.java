/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum RoleTreeDataTypeEnum implements EnumDto<Integer> {

	ROLE_INFO(0), USER_INFO(1);

	private final Integer code;

	private RoleTreeDataTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}

}
