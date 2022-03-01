/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype;

import com.clustercontrol.monitor.run.bean.MonitorPredictionMethod;
import com.clustercontrol.rest.dto.EnumDto;

public enum PredictionMethodEnum implements EnumDto<String> {

	POLYNOMIAL_1(MonitorPredictionMethod.POLYNOMIAL_1),
	POLYNOMIAL_2(MonitorPredictionMethod.POLYNOMIAL_2),
	POLYNOMIAL_3(MonitorPredictionMethod.POLYNOMIAL_3);

	private final String code;

	private PredictionMethodEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
