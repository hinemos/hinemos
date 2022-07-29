/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ConfirmFlgTypeEnum implements EnumDto<Integer>{
	//（未確認／確認済／確認中）
	TYPE_UNCONFIRMED(0), TYPE_CONFIRMED(1),TYPE_CONFIRMING(2);

	private final Integer code;

	private ConfirmFlgTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
