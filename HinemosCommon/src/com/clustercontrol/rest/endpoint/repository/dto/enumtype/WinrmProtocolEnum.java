/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.bean.WinrmProtocolConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum WinrmProtocolEnum implements EnumDto<String> {

	NONE(""),
	HTTP(WinrmProtocolConstant.HTTP),
	HTTPS(WinrmProtocolConstant.HTTPS);

	private final String code;

	private WinrmProtocolEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
