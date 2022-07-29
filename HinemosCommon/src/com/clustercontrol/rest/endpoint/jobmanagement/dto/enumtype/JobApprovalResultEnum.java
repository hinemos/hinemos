/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.bean.JobApprovalResultConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JobApprovalResultEnum implements EnumDto<Integer> {

	/** 承認 */
	APPROVAL(JobApprovalResultConstant.TYPE_APPROVAL),

	/** 否認 */
	DENIAL(JobApprovalResultConstant.TYPE_DENIAL);

	private final Integer code;

	private JobApprovalResultEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
