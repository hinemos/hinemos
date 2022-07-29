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
 * ターゲット種別
 */
public enum StatusFacilityTargetEnum implements EnumDto<Integer> {

	ONE_LEVEL(0),
	ALL(1);

	private final Integer code;

	private StatusFacilityTargetEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
