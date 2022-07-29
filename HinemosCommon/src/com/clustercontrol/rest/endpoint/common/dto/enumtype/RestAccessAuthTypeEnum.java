/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto.enumtype;

import com.clustercontrol.commons.bean.RestAccessAuthTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RestAccessAuthTypeEnum implements EnumDto<Integer> {
	NONE(RestAccessAuthTypeConstant.TYPE_NONE), 
	BASIC(RestAccessAuthTypeConstant.TYPE_BASIC), 
	URL(RestAccessAuthTypeConstant.TYPE_URL);
	
	private final Integer code;

	private RestAccessAuthTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}