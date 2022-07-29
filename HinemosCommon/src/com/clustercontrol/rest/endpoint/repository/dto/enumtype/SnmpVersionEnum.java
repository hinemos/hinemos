/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum SnmpVersionEnum implements EnumDto<Integer> {

	TYPE_V1(SnmpVersionConstant.TYPE_V1),
	TYPE_V2(SnmpVersionConstant.TYPE_V2),
	TYPE_V3(SnmpVersionConstant.TYPE_V3);

	private final Integer code;

	private SnmpVersionEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
