/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.agent.dto;

import java.util.List;

import com.clustercontrol.rest.endpoint.agent.dto.AgentInfoRequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgentRequestDto;

public class ForwardSdmlControlLogRequest extends AgentRequestDto {
	List<AgtSdmlControlLogDTORequest> logList;
	AgentInfoRequest agentInfo;

	public ForwardSdmlControlLogRequest() {
	}

	public List<AgtSdmlControlLogDTORequest> getLogList() {
		return logList;
	}

	public void setLogList(List<AgtSdmlControlLogDTORequest> logList) {
		this.logList = logList;
	}

	public AgentInfoRequest getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfoRequest agentInfo) {
		this.agentInfo = agentInfo;
	}
}
