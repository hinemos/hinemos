/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.rest.dto.EnumDto;

public enum JobOutputType implements EnumDto<Integer> {

	STDOUT(0),
	STDERR(1);

	private final Integer code;

	private JobOutputType(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
