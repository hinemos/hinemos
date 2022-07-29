/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum BinaryLengthTypeEnum implements EnumDto<String> {

	VARIABLE(BinaryConstant.LENGTH_TYPE_VARIABLE), FIXED(BinaryConstant.LENGTH_TYPE_FIXED);

	private final String code;

	private BinaryLengthTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
