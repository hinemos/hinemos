/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto.enumtype;

import com.clustercontrol.bean.HttpMethodTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum HttpMethodTypeEnum implements EnumDto<Integer> {
	GET(HttpMethodTypeConstant.TYPE_GET), 
	POST(HttpMethodTypeConstant.TYPE_POST), 
	PUT(HttpMethodTypeConstant.TYPE_PUT), 
	DELETE(HttpMethodTypeConstant.TYPE_DELETE);
	
	private final Integer code;

	private HttpMethodTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}

}