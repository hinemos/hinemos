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

public enum BinaryTimeStampTypeEnum implements EnumDto<String> {

	SEC_AND_USEC(BinaryConstant.TS_TYPE_SEC_AND_USEC), ONLY_SEC(BinaryConstant.TS_TYPE_ONLY_SEC);

	private final String code;

	private BinaryTimeStampTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
