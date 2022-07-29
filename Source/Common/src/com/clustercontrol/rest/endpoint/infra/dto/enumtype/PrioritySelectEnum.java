/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum PrioritySelectEnum implements EnumDto<Integer> {
	/** 危険（種別）。 */TYPE_CRITICAL(0),
	/** 警告（種別）。 */TYPE_WARNING(2),
	/** 通知（種別）。 */TYPE_INFO(3),
	/** 不明（種別）。 */TYPE_UNKNOWN(1),
	/** 無し（種別）。 */TYPE_NONE(4);
	private final Integer code;

	private PrioritySelectEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
