/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.collect.dto.emuntype;

import com.clustercontrol.rest.dto.EnumDto;

public enum SummaryTypeEnum implements EnumDto<Integer>{
	
	TYPE_RAW(0),
	TYPE_AVG_HOUR(1),
	TYPE_AVG_DAY(2),
	TYPE_AVG_MONTH(3),
	TYPE_MIN_HOUR(4),
	TYPE_MIN_DAY(5),
	TYPE_MIN_MONTH(6),
	TYPE_MAX_HOUR (7),
	TYPE_MAX_DAY(8),
	TYPE_MAX_MONTH(9);

	private final Integer code;

	private SummaryTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}

