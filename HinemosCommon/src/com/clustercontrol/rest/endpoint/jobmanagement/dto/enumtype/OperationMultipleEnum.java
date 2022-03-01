/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum OperationMultipleEnum  implements EnumDto<Integer> {
	/** 待機(状態の種別) */ WAIT(StatusConstant.TYPE_WAIT),
	/** 終了(状態の種別) */ END(StatusConstant.TYPE_END);
	
	private final Integer code;

	private OperationMultipleEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	

}
