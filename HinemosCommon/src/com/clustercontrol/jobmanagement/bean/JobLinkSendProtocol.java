/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.rest.dto.EnumDto;

public enum JobLinkSendProtocol implements EnumDto<String> {

	HTTP("http"),
	HTTPS("https");

	private final String code;

	private JobLinkSendProtocol(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
