/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RpaJobTypeEnum implements EnumDto<Integer> {
	DIRECT(RpaJobTypeConstant.DIRECT),
	INDIRECT(RpaJobTypeConstant.INDIRECT);

	private final Integer code;

	private RpaJobTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
