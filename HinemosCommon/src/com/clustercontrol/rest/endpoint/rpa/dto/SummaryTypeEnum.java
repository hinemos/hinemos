/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto;

public enum SummaryTypeEnum{
	
	TYPE_DAILY_COUNT(0),
	TYPE_HOURLY_REDUCTION(1),
	TYPE_SCENARIO_ERRORS(2),
	TYPE_NODE_ERRORS(3),
	TYPE_SCENARIO_REDUCTION(4),
	TYPE_NODE_REDUCTION(5),
	TYPE_ERRORS(6),
	TYPE_REDUCTION(7);

	private final Integer code;

	private SummaryTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}

