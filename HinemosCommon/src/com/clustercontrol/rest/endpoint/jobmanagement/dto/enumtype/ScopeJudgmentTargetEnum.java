/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum ScopeJudgmentTargetEnum implements EnumDto<Integer> {
	ALL_NODE(ProcessingMethodConstant.TYPE_ALL_NODE),
	ANY_NODE(ProcessingMethodConstant.TYPE_ANY_NODE);

	private final Integer code;

	private ScopeJudgmentTargetEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
