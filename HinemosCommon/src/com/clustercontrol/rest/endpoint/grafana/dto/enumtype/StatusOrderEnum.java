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
 * 並び順種別
 */
public enum StatusOrderEnum implements EnumDto<Integer> {

	ASC(0), 
	DESC(1);

	private final Integer code;

	private StatusOrderEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
