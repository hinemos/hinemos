/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JobApprovalStatusEnum implements EnumDto<Integer> {

	/** 承認済(状態の種別) */
	FINISHED(JobApprovalStatusConstant.TYPE_FINISHED),

	/** 停止(状態の種別) */
	STOP(JobApprovalStatusConstant.TYPE_STOP),

	/** 中断中(状態の種別) */
	SUSPEND(JobApprovalStatusConstant.TYPE_SUSPEND),

	/** 未承認(状態の種別) */
	STILL(JobApprovalStatusConstant.TYPE_STILL),

	/** 承認待(状態の種別) */
	PENDING(JobApprovalStatusConstant.TYPE_PENDING);

	private final Integer code;

	private JobApprovalStatusEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
