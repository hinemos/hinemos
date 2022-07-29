/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JobParamTypeEnum implements EnumDto<Integer> {
	/** システムパラメータ（ジョブ） */ SYSTEM_JOB(JobParamTypeConstant.TYPE_SYSTEM_JOB),
	/** システムパラメータ（ノード） */ SYSTEM_NODE(JobParamTypeConstant.TYPE_SYSTEM_NODE),
	/** ユーザパラメータ */ USER(JobParamTypeConstant.TYPE_USER),
	/** ランタイムパラメータ */ RUNTIME(JobParamTypeConstant.TYPE_RUNTIME);
	
	private final Integer code;

	private JobParamTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	

}
