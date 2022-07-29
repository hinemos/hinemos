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
 * グループ化種別
 */
public enum EventGroupByEnum implements EnumDto<String> {

	// ・memo
	// EnumDto<String>にして文字列(ex. PRIORITY("PRIORITY"))と定義
	// していただいてもいいです
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

	private EventGroupByEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
