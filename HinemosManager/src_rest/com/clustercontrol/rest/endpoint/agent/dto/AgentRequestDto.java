/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public abstract class AgentRequestDto implements RequestDto {

	public AgentRequestDto() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// なにもしない
	}

}
