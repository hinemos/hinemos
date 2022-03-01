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
public enum StatusGroupByEnum implements EnumDto<String> {

	// ・memo
	// EnumDto<String>にして文字列(ex. PRIORITY("PRIORITY"))と定義
	// していただいてもいいです
	PRIORITY("priority"), 
	FACILITY_ID("facility_id"), 
	MONITOR_ID("monitor_id"),
	APPLICATION("application");

	private final String code;

	private StatusGroupByEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
