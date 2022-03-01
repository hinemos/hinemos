/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ReportOutputPeriodTypeEnum implements EnumDto<Integer> {

	OUTPUT_PERIOD_TYPE_DAY(0), OUTPUT_PERIOD_TYPE_MONTH(1), OUTPUT_PERIOD_TYPE_YEAR(2);

	private final Integer code;

	private ReportOutputPeriodTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
