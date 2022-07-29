/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum MonitorTypeEnum implements EnumDto<Integer> {

	TRUTH(MonitorTypeConstant.TYPE_TRUTH),
	NUMERIC(MonitorTypeConstant.TYPE_NUMERIC),
	STRING(MonitorTypeConstant.TYPE_STRING),
	TRAP(MonitorTypeConstant.TYPE_TRAP),
	SCENARIO(MonitorTypeConstant.TYPE_SCENARIO),
	BINARY(MonitorTypeConstant.TYPE_BINARY);

	private final Integer code;

	private MonitorTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
