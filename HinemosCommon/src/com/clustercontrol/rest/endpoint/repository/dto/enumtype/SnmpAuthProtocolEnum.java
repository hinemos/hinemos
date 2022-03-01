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

public enum SnmpAuthProtocolEnum implements EnumDto<String> {

	/** 未設定 */
	NONE(""),
	/** MD5 */
	MD5(SnmpProtocolConstant.MD5),
	/** SHA */
	SHA(SnmpProtocolConstant.SHA),
	/** SHA-224 */
	SHA224(SnmpProtocolConstant.SHA224),
	/** SHA-256 */
	SHA256(SnmpProtocolConstant.SHA256),
	/** SHA-384 */
	SHA384(SnmpProtocolConstant.SHA384),
	/** SHA-512 */
	SHA512(SnmpProtocolConstant.SHA512);

	private final String code;

	private SnmpAuthProtocolEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
