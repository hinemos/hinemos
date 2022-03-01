/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RpaJobReturnCodeConditionEnum implements EnumDto<Integer> {
	/** =(数値) */
	EQUAL_NUMERIC(RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC),
	/** !=(数値) */
	NOT_EQUAL_NUMERIC(RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC),
	/** &gt;(数値) */
	GREATER_THAN(RpaJobReturnCodeConditionConstant.GREATER_THAN),
	/** &gt;=(数値) */
	GREATER_THAN_OR_EQUAL_TO(RpaJobReturnCodeConditionConstant.GREATER_THAN_OR_EQUAL_TO),
	/** &lt;(数値) */
	LESS_THAN(RpaJobReturnCodeConditionConstant.LESS_THAN),
	/** &lt;=(数値) */
	LESS_THAN_OR_EQUAL_TO(RpaJobReturnCodeConditionConstant.LESS_THAN_OR_EQUAL_TO);

	private final Integer code;

	private RpaJobReturnCodeConditionEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
