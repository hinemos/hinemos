/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.rpa.bean.RpaJobErrorTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RpaJobErrorTypeEnum implements EnumDto<Integer> {

	/** ログインされていない（RPAツールエグゼキューターが起動していない ） */
	NOT_LOGIN(RpaJobErrorTypeConstant.NOT_LOGIN),
	/** RPAツールが既に起動している */
	ALREADY_RUNNING(RpaJobErrorTypeConstant.ALREADY_RUNNING),
	/** RPAツールが異常終了した */
	ABNORMAL_EXIT(RpaJobErrorTypeConstant.ABNORMAL_EXIT),
	/** それ以外 */
	OTHER(RpaJobErrorTypeConstant.OTHER);

	private final Integer code;

	private RpaJobErrorTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}

}
