/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum NodeRegisterFlagEnum implements EnumDto<Integer> {

	GET_SUCCESS(Integer.valueOf(NodeRegisterFlagConstant.GET_SUCCESS)),
	GET_FAILURE(Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE)),
	NOT_GET(Integer.valueOf(NodeRegisterFlagConstant.NOT_GET));

	private final Integer code;

	private NodeRegisterFlagEnum(final Integer code) {
		this.code = code;
	}
	public Integer getCode() {
		return code;
	}
}
