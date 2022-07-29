/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum OperationFailureEnum implements EnumDto<Integer> {
	/** 停止[中断] */ STOP_SUSPEND(OperationConstant.TYPE_STOP_SUSPEND),
	/** 停止[状態指定] */ STOP_SET_END_VALUE(OperationConstant.TYPE_STOP_SET_END_VALUE);

	private final Integer code;

	private OperationFailureEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	

}
