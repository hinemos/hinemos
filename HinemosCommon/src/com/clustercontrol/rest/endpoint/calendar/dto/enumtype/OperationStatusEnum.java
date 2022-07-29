/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum OperationStatusEnum implements EnumDto<Integer> {

	ALL_OPERATION(0), PARTIAL_OPERATION(1), NOT_OPERATION(2);

	private final Integer code;

	private OperationStatusEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
	
}
