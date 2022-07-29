/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum ReportOutputTypeEnum implements EnumDto<Integer> {

	pdf(0), xlsx(1), xls(2);

	private final Integer code;

	private ReportOutputTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
