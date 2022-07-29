/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum TriggerTypeEnum implements EnumDto<String> {

	CRON("CRON"),
	SIMPLE("SIMPLE"),
	NONE("NONE");

	private final String code;

	private TriggerTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
