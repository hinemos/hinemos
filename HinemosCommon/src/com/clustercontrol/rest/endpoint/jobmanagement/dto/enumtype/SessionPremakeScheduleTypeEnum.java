/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.SessionPremakeScheduleType;
import com.clustercontrol.rest.dto.EnumDto;

public enum SessionPremakeScheduleTypeEnum implements EnumDto<Integer> {
	EVERY_DAY(SessionPremakeScheduleType.TYPE_EVERY_DAY),
	EVERY_WEEK(SessionPremakeScheduleType.TYPE_EVERY_WEEK),
	TIME(SessionPremakeScheduleType.TYPE_TIME),
	DATETIME(SessionPremakeScheduleType.TYPE_DATETIME);

	private final Integer code;

	private SessionPremakeScheduleTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	

}
