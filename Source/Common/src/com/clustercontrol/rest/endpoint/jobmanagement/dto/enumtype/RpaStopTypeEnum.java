/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.RpaStopTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RpaStopTypeEnum implements EnumDto<Integer> {
	/** シナリオを終了する */
	STOP_SCENARIO(RpaStopTypeConstant.STOP_SCENARIO),
	/** シナリオは終了せず、ジョブのみ終了する */
	STOP_JOB(RpaStopTypeConstant.STOP_JOB);

	private final Integer code;

	private RpaStopTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
