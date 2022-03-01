/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum ScheduleTypeEnum implements EnumDto<Integer> {
	/** [毎日]・時・分の場合 */
	DAY(ScheduleConstant.TYPE_DAY),
	/** [週]・時・分の場合 */
	WEEK (ScheduleConstant.TYPE_WEEK),
	/** p分からq分毎に繰り返し実行の場合 */
	REPEAT(ScheduleConstant.TYPE_REPEAT),
	/** [一定間隔]・時・分から・q分毎に繰り返し実行の場合 */
	INTERVAL(ScheduleConstant.TYPE_INTERVAL);

	private final Integer code;

	private ScheduleTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
