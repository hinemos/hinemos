/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum ExecFacilityFlgEnum implements EnumDto<Integer> {

	GENERATION(ExecFacilityConstant.TYPE_GENERATION),
	FIX(ExecFacilityConstant.TYPE_FIX);

	private final Integer code;

	private ExecFacilityFlgEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
