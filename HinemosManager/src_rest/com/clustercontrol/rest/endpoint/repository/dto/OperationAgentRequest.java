/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.AgentCommandEnum;

public class OperationAgentRequest implements RequestDto {

	private List<String> facilityIds = new ArrayList<>();
	@RestBeanConvertEnum
	private AgentCommandEnum agentCommand;

	public OperationAgentRequest() {
	}

	public List<String> getFacilityIds() {
		return facilityIds;
	}

	public void setFacilityIds(List<String> facilityIds) {
		this.facilityIds = facilityIds;
	}

	public AgentCommandEnum getAgentCommand() {
		return agentCommand;
	}

	public void setAgentCommand(AgentCommandEnum agentCommand) {
		this.agentCommand = agentCommand;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
