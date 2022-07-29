/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JobRuntimeParamTypeEnum implements EnumDto<Integer> {
	/** 入力の場合 */INPUT(JobRuntimeParamTypeConstant.TYPE_INPUT),
	/** 選択（ラジオボタン）の場合 */RADIO(JobRuntimeParamTypeConstant.TYPE_RADIO),
	/** 選択（コンボボックス）の場合 */COMBO(JobRuntimeParamTypeConstant.TYPE_COMBO),
	/** 固定の場合 */FIXED(JobRuntimeParamTypeConstant.TYPE_FIXED);

	private final Integer code;

	private JobRuntimeParamTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
