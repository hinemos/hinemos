/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto.enumtype;

import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum HinemosPropertyTypeEnum implements EnumDto<Integer> {
	STRING(HinemosPropertyTypeConstant.TYPE_STRING), NUMERIC(HinemosPropertyTypeConstant.TYPE_NUMERIC), BOOLEAN(
			HinemosPropertyTypeConstant.TYPE_TRUTH);

	private final Integer code;

	private HinemosPropertyTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}

}