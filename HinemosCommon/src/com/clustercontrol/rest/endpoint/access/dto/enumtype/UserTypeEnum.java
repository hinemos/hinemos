/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto.enumtype;

import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum UserTypeEnum implements EnumDto<String> {

	SYSTEM_USER(UserTypeConstant.SYSTEM_USER), LOGIN_USER(UserTypeConstant.LOGIN_USER), INTERNAL_USER(
			UserTypeConstant.INTERNAL_USER);

	private final String code;

	private UserTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
