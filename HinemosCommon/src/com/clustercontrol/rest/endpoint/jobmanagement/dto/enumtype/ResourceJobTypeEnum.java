/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ResourceJobTypeEnum implements EnumDto<Integer> {

	/** コンピュート - ファシリティＩＤ */
	COMPUTE_FACILITY_ID(0),
	/** コンピュート - コンピュートＩＤ */
	COMPUTE_COMPUTE_ID(1),
	/** ストレージ */
	STORAGE(2);

	private Integer code;

	private ResourceJobTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
