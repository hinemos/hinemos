/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum SnmpSecurityLevelEnum implements EnumDto<String> {

	/** 未設定 */
	NONE(""),
	/** 認証なし・暗号化なし */
	NOAUTH_NOPRIV(SnmpSecurityLevelConstant.NOAUTH_NOPRIV),
	/** 認証あり・暗号化なし */
	AUTH_NOPRIV(SnmpSecurityLevelConstant.AUTH_NOPRIV),
	/** 認証あり・暗号化あり */
	AUTH_PRIV(SnmpSecurityLevelConstant.AUTH_PRIV);

	private final String code;

	private SnmpSecurityLevelEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
