/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum PriorityRequiredEnum implements EnumDto<Integer> {
	/** 危険（種別）。 */CRITICAL(PriorityConstant.TYPE_CRITICAL),
	/** 警告（種別）。 */WARNING(PriorityConstant.TYPE_WARNING),
	/** 通知（種別）。 */INFO(PriorityConstant.TYPE_INFO),
	/** 不明（種別）。 */UNKNOWN(PriorityConstant.TYPE_UNKNOWN);

	private final Integer code;

	private PriorityRequiredEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
