/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.monitor.bean.PriorityChangeJudgmentTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum PriorityChangeJudgmentTypeEnum implements EnumDto<Integer> {
	NOT_PRIORITY_CHANGE(PriorityChangeJudgmentTypeConstant.TYPE_NOT_PRIORITY_CHANGE ),
	ACROSS_MONITOR_DETAIL_ID(PriorityChangeJudgmentTypeConstant.TYPE_ACROSS_MONITOR_DETAIL_ID );

	private final Integer code;

	private PriorityChangeJudgmentTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}