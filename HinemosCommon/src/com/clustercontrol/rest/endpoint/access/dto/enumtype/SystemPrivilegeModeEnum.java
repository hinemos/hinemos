/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum SystemPrivilegeModeEnum implements EnumDto<String> {
	ADD("ADD"), READ("READ"), MODIFY("MODIFY"), EXEC("EXEC"), APPROVAL("APPROVAL");

	private final String code;

	private SystemPrivilegeModeEnum(final String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

}
