/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.NodeConfigRunInterval;
import com.clustercontrol.rest.dto.EnumDto;

public enum NodeConfigRunIntervalEnum implements EnumDto<Integer> {

	HOUR_6(NodeConfigRunInterval.TYPE_HOUR_6.toSec()),
	HOUR_12(NodeConfigRunInterval.TYPE_HOUR_12.toSec()),
	HOUR_24(NodeConfigRunInterval.TYPE_HOUR_24.toSec());

	private final Integer code;

	private NodeConfigRunIntervalEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
