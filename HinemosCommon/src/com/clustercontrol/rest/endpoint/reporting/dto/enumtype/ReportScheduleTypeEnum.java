/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ReportScheduleTypeEnum implements EnumDto<Integer> {
	/** 毎日・時・分の場合 */
	DAY(1),
	/** 週・時・分の場合 */
	WEEK (2),
	/** p分からq分毎に繰り返し実行の場合 */
	REPEAT(3);
	
	private final Integer code;

	private ReportScheduleTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
