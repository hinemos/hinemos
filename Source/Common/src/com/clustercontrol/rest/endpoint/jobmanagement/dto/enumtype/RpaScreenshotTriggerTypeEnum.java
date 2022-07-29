/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.rpa.bean.RpaScreenshotTriggerTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RpaScreenshotTriggerTypeEnum implements EnumDto<Integer> {
	/** 終了遅延 */
	END_DELAY(RpaScreenshotTriggerTypeConstant.END_DELAY),
	/** 終了値 */
	END_VALUE(RpaScreenshotTriggerTypeConstant.END_VALUE),
	/** 手動実行 */
	MANUAL(RpaScreenshotTriggerTypeConstant.MANUAL);

	private final Integer code;

	private RpaScreenshotTriggerTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
