/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.calendar.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum DayTypeEnum implements EnumDto<Integer> {

	ALL_DAY(0), DAY_OF_WEEK(1), DAY(2), CALENDAR_PATTERN(3);

	private final Integer code;

	private DayTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
