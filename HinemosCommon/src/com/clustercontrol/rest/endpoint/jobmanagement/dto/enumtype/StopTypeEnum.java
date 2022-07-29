/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum StopTypeEnum implements EnumDto<Integer> {

	EXECUTE_COMMAND(CommandStopTypeConstant.EXECUTE_COMMAND),
	DESTROY_PROCESS(CommandStopTypeConstant.DESTROY_PROCESS);

	private final Integer code;

	private StopTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	

}
