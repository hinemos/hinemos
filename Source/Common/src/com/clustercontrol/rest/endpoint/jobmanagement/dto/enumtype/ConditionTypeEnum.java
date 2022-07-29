/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum ConditionTypeEnum implements EnumDto<Integer> {

	/** AND条件（数値） */
	AND(ConditionTypeConstant.TYPE_AND),
	/** OR条件（数値） */
	OR(ConditionTypeConstant.TYPE_OR);
	private final Integer code;

	private ConditionTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
