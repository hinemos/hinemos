/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ReferJobSelectTypeEnum implements EnumDto<Integer> {
	JOB_TREE(0),
	REGISTERED_MODULE(1);
	
	private final Integer code;
	

	private ReferJobSelectTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
