/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum MonitorNumericTypeEnum implements EnumDto<String> {

	BASIC(""), PREDICTION("PREDICTION"), CHANGE("CHANGE");

	private final String code;

	private MonitorNumericTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
