/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;
import com.clustercontrol.rpa.bean.RpaManagementToolRunParamTypeConstant;

public enum RpaManagementToolRunParamTypeEnum implements EnumDto<Integer> {
	STRING(RpaManagementToolRunParamTypeConstant.TYPE_STRING),
	NUMERIC(RpaManagementToolRunParamTypeConstant.TYPE_NUMERIC),
	BOOLEAN(RpaManagementToolRunParamTypeConstant.TYPE_BOOLEAN);

	private final Integer code;

	private RpaManagementToolRunParamTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
