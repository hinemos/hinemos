/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;

public enum PortServiceProtocolEnum implements EnumDto<String> {

	TCP("001"),
	FTP("002"),
	SMTP("003"),
	SMTPS("004"),
	POP3("005"),
	POP3S("006"),
	IMAP("007"),
	IMAPS("008"),
	NTP("009"),
	DNS("010");

	private final String code;

	private PortServiceProtocolEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
