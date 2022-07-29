/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.rpa.bean.RpaJobEndValueConditionTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RpaJobEndValueConditionTypeEnum implements EnumDto<Integer> {
	/** ログから判定 */
	LOG(RpaJobEndValueConditionTypeConstant.LOG),
	/** リターンコードから判定 */
	RETURN_CODE(RpaJobEndValueConditionTypeConstant.RETURN_CODE);

	private RpaJobEndValueConditionTypeEnum(Integer code) {
		this.code = code;
	}

	private final Integer code;

	@Override
	public Integer getCode() {
		return code;
	}
}
