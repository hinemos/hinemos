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

public enum ProcessingMethodEnum implements EnumDto<Integer> {
	ALL_NODE(ProcessingMethodConstant.TYPE_ALL_NODE),
	RETRY(ProcessingMethodConstant.TYPE_RETRY);

	private final Integer code;

	private ProcessingMethodEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
