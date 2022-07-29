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

public enum BinaryCutTypeEnum implements EnumDto<String> {

	INTERVAL(BinaryConstant.CUT_TYPE_INTERVAL), LENGTH(BinaryConstant.CUT_TYPE_LENGTH);

	private final String code;

	private BinaryCutTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
