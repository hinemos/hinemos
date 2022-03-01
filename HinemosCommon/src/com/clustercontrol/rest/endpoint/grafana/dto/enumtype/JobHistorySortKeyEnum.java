/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

/**
 * ソート種別
 */
public enum JobHistorySortKeyEnum implements EnumDto<String> {

	COUNT("count"),
	STATUS("status"),
	END_STATUS("end_status"),
	JOB_ID("job_id"),
	TRIGGER_INFO("trigger_info"),
	TRIGGER_TYPE("trigger_type"),
	START_HOUR("start_hour"),
	START_DAY("start_day"),
	START_MONTH("start_month"),
	START_YEAR("start_year"),
	END_HOUR("end_hour"),
	END_DAY("end_day"),
	END_MONTH("end_month"),
	END_YEAR("end_year");

	private final String code;

	private JobHistorySortKeyEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
