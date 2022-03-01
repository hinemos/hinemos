/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ScenarioCreateIntervalEnum implements EnumDto<Integer> {
	SEC_30(30),
	MIN_01(60),
	MIN_05(300),
	MIN_10(600),
	MIN_30(1800),
	MIN_60(3600),
	;

	private Integer interval;
	ScenarioCreateIntervalEnum(Integer interval) {
		this.interval = interval;
	}

	@Override
	public Integer getCode() {
		return interval;
	}
	
}
