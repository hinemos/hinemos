/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto.enumtype;

import com.clustercontrol.accesscontrol.bean.RoleTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RoleTypeEnum implements EnumDto<String> {

	SYSTEM_ROLE(RoleTypeConstant.SYSTEM_ROLE), USER_ROLE(RoleTypeConstant.USER_ROLE), INTERNAL_ROLE(
			RoleTypeConstant.INTERNAL_ROLE);

	private final String code;

	private RoleTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
