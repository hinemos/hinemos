/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.bean.WbemProtocolConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum WbemProtocolEnum implements EnumDto<String> {

	NONE(""),
	HTTP(WbemProtocolConstant.HTTP),
	HTTPS(WbemProtocolConstant.HTTPS);

	private final String code;

	private WbemProtocolEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
