/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JudgmentObjectEnum implements EnumDto<Integer> {
	/** ジョブ終了状態 */JOB_END_STATUS(JudgmentObjectConstant.TYPE_JOB_END_STATUS),
	/** ジョブ終了値 */JOB_END_VALUE(JudgmentObjectConstant.TYPE_JOB_END_VALUE),
	/** 時刻 */TIME(JudgmentObjectConstant.TYPE_TIME),
	/** セッション開始時の時間（分）  */START_MINUTE(JudgmentObjectConstant.TYPE_START_MINUTE),
	/** ジョブ変数 */JOB_PARAMETER(JudgmentObjectConstant.TYPE_JOB_PARAMETER),
	/** セッション横断ジョブ終了状態 */CROSS_SESSION_JOB_END_STATUS(JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS),
	/** セッション横断ジョブ終了値 */CROSS_SESSION_JOB_END_VALUE(JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE),
	/** ジョブ戻り値 */JOB_RETURN_VALUE(JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE);
	private final Integer code;

	private JudgmentObjectEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
