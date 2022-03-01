/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.monitor.bean.PriorityChangeFailureTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum PriorityChangeFailureTypeEnum implements EnumDto<Integer> {
	NOT_PRIORITY_CHANGE(PriorityChangeFailureTypeConstant.TYPE_NOT_PRIORITY_CHANGE),
	PRIORITY_CHANGE(PriorityChangeFailureTypeConstant.TYPE_PRIORITY_CHANGE);

	private final Integer code;

	private PriorityChangeFailureTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}