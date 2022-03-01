/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class ForwardLogfileResultRequest extends AgentRequestDto {
	List<AgtLogfileResultDTORequest> resultList;
	AgentInfoRequest agentInfo;

	public ForwardLogfileResultRequest() {
	}

	public List<AgtLogfileResultDTORequest> getResultList() {
		return resultList;
	}

	public void setResultList(List<AgtLogfileResultDTORequest> resultList) {
		this.resultList = resultList;
	}

	public AgentInfoRequest getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfoRequest agentInfo) {
		this.agentInfo = agentInfo;
	}

}
