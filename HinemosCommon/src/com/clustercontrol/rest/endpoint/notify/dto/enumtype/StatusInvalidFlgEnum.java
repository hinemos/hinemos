/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.monitor.bean.StatusExpirationConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum StatusInvalidFlgEnum implements EnumDto<Integer> {

	DELETE(StatusExpirationConstant.TYPE_DELETE),
	UPDATE(StatusExpirationConstant.TYPE_UPDATE);

	private final Integer code;

	private StatusInvalidFlgEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
