/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.hub.dto.enumtype;

import java.util.Arrays;
import java.util.List;

import com.clustercontrol.rest.dto.EnumDto;

public enum TransferIntervalEnum implements EnumDto<Integer> {

	BATCH_HOUR_1(1),
	BATCH_HOUR_3(3),
	BATCH_HOUR_6(6),
	BATCH_HOUR_12(12),
	BATCH_HOUR_24(24),
	DELAY_DAY_10(10),
	DELAY_DAY_20(20),
	DELAY_DAY_30(30),
	DELAY_DAY_60(60),
	DELAY_DAY_90(90);
	
	private final Integer code;

	private TransferIntervalEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

	public static List<TransferIntervalEnum> getBatchValues() {
		return  Arrays.asList(BATCH_HOUR_1, BATCH_HOUR_3, BATCH_HOUR_6, BATCH_HOUR_12, BATCH_HOUR_24);
	}
	
	public static List<TransferIntervalEnum> getDelayValues() {
		return  Arrays.asList(DELAY_DAY_10, DELAY_DAY_20, DELAY_DAY_30, DELAY_DAY_60, DELAY_DAY_90);
	}
}
