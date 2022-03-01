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

public enum OperationStartDelayEnum  implements EnumDto<Integer> {
	/**  停止[コマンド] */ STOP_AT_ONCE(OperationConstant.TYPE_STOP_AT_ONCE),
	/** 停止[スキップ] */ STOP_SKIP(OperationConstant.TYPE_STOP_SKIP),
	/** 停止[保留] */ STOP_WAIT (OperationConstant.TYPE_STOP_WAIT);
	
	private final Integer code;

	private OperationStartDelayEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
