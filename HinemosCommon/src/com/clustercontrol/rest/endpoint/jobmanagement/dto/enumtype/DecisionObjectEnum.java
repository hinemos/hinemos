/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.DecisionObjectConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum DecisionObjectEnum implements EnumDto<Integer> {

	/** =(数値) */ EQUAL_NUMERIC(DecisionObjectConstant.EQUAL_NUMERIC),
	/** !=(数値) */ NOT_EQUAL_NUMERIC(DecisionObjectConstant.NOT_EQUAL_NUMERIC),
	/** >(数値) */ GREATER_THAN(DecisionObjectConstant.GREATER_THAN),
	/** >=(数値) */ GREATER_THAN_OR_EQUAL_TO(DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO),
	/** <(数値) */ LESS_THAN(DecisionObjectConstant.LESS_THAN),
	/** <=(数値) */ LESS_THAN_OR_EQUAL_TO(DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO),
	/** =(文字列) */ EQUAL_STRING(DecisionObjectConstant.EQUAL_STRING),
	/** !=(文字列) */ NOT_EQUAL_STRING(DecisionObjectConstant.NOT_EQUAL_STRING),
	/** IN(数値) */ IN_NUMERIC(DecisionObjectConstant.IN_NUMERIC),
	/** NOT IN(数値) */ NOT_IN_NUMERIC(DecisionObjectConstant.NOT_IN_NUMERIC);

	private final Integer code;

	private DecisionObjectEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	

}
