/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum RunIntervalEnum implements EnumDto<Integer> {

	NONE(0), SEC_30(30),MIN_01(60),MIN_05(300),MIN_10(600),MIN_30(1800),MIN_60(3600);

		
	private final Integer code;

	private RunIntervalEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
