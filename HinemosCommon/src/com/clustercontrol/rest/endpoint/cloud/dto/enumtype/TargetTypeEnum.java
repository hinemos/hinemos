/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum TargetTypeEnum implements EnumDto<String> {
	facility("facility"),
	instance("instance");
	
	private String code;
	private TargetTypeEnum(String code) {
		this.code = code;
	}
	@Override
	public String getCode() {
		return code;
	}
}
