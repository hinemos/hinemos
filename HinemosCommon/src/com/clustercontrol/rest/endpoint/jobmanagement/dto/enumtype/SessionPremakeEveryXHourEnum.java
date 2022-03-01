/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum SessionPremakeEveryXHourEnum implements EnumDto<Integer> {

	HOUR_1(1),HOUR_2(2),HOUR_3(3),HOUR_4(4),HOUR_6(6),HOUR_8(8), HOUR_12(12);

	private final Integer code;

	private SessionPremakeEveryXHourEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
