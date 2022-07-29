/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto.enumtype;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum SystemPrivilegeEditTypeEnum implements EnumDto<String> {

	SYSTEMPRIVILEGE_EDITTYPE_NONE(PrivilegeConstant.SYSTEMPRIVILEGE_EDITTYPE_NONE), SYSTEMPRIVILEGE_EDITTYPE_DIALOG(
			PrivilegeConstant.SYSTEMPRIVILEGE_EDITTYPE_DIALOG);

	private final String code;

	private SystemPrivilegeEditTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
