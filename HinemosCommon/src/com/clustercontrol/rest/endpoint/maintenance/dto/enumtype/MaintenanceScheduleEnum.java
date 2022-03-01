/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.maintenance.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum MaintenanceScheduleEnum implements EnumDto<Integer> {
	/** 月・日・時・分の場合 */
	DAY(1),
	/** 週・時・分の場合 */
	WEEK (2);
	
	private final Integer code;

	private MaintenanceScheduleEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
