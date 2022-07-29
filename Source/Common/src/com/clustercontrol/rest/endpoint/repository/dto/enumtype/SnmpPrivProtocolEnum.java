/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum SnmpPrivProtocolEnum implements EnumDto<String> {

	/** 未設定 */
	NONE(""),
	/** DES */
	DES(SnmpProtocolConstant.DES),
	/** AES */
	AES(SnmpProtocolConstant.AES),
	/** AES-192 */
	AES192(SnmpProtocolConstant.AES192),
	/** AES-256 */
	AES256(SnmpProtocolConstant.AES256);

	private final String code;

	private SnmpPrivProtocolEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
