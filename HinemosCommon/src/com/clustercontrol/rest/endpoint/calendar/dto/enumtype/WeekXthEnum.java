/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.calendar.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum WeekXthEnum implements EnumDto<Integer> {

	EVERY_WEEK(0), FIRST_WEEK(1), SECOND_WEEK(2), THIRD_WEEK(3), FOURTH_WEEK(4), FIFTH_WEEK(5);

	private final Integer code;

	private WeekXthEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}

}
