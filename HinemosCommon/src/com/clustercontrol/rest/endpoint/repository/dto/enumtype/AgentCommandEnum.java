/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum AgentCommandEnum implements EnumDto<Integer> {

	RESTART(AgentCommandConstant.RESTART), UPDATE(AgentCommandConstant.UPDATE);

	private final Integer code;

	private AgentCommandEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
