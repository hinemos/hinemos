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

public enum BinaryCollectTypeEnum implements EnumDto<String> {

	WHOLE_FILE(BinaryConstant.COLLECT_TYPE_WHOLE_FILE), ONLY_INCREMENTS(BinaryConstant.COLLECT_TYPE_ONLY_INCREMENTS);

	private final String code;

	private BinaryCollectTypeEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}