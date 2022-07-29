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
public enum EventSortKeyEnum implements EnumDto<String> {

	COUNT("count"), 
	PRIORITY("priority"), 
	FACILITY_ID("facility_id"), 
	MONITOR_ID("monitor_id"),
	APPLICATION("application"),
	CONFIRM("confirm_flg"),
	OUTPUT_HOUR("output_hour"),
	OUTPUT_DAY("output_day"),
	OUTPUT_MONTH("output_month"),
	OUTPUT_YEAR("output_year"),
	GENERATION_HOUR("generation_hour"),
	GENERATION_DAY("generation_day"),
	GENERATION_MONTH("generation_month"),
	GENERATION_YEAR("generation_year");

	private final String code;

	private EventSortKeyEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
