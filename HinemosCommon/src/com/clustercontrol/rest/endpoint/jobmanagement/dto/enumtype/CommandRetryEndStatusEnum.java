/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum CommandRetryEndStatusEnum implements EnumDto<Integer> {
	NORMAL(EndStatusConstant.TYPE_NORMAL),
	WARNING(EndStatusConstant.TYPE_WARNING),
	ABNORMAL(EndStatusConstant.TYPE_ABNORMAL),
	ANY(EndStatusConstant.TYPE_ANY);

	private final Integer code;

	private CommandRetryEndStatusEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	

}
