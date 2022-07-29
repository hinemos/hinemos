/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ConfiremTypeEnum implements EnumDto<Integer>{
	//確認タイプ（未確認／確認済／確認中）
	CONFIRMED_UNCONFIRMED(0), CONFIRMED_CONFIRMED(1),CONFIRMED_CONFIRMING(2);

	private final Integer code;

	private ConfiremTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
