/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype;

import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum FacilityTypeEnum implements EnumDto<Integer>{
	
	TYPE_BENEATH(FacilityTargetConstant.TYPE_BENEATH),
	TYPE_ALL(FacilityTargetConstant.TYPE_ALL);

	private final Integer code;

	private FacilityTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
