/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class ForwardWinEventResultRequest extends AgentRequestDto {

	private List<AgtWinEventResultDTORequest> resultList;
	private AgentInfoRequest agentInfo;

	public ForwardWinEventResultRequest() {
	}

	public List<AgtWinEventResultDTORequest> getResultList() {
		return resultList;
	}

	public void setResultList(List<AgtWinEventResultDTORequest> resultList) {
		this.resultList = resultList;
	}

	public AgentInfoRequest getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfoRequest agentInfo) {
		this.agentInfo = agentInfo;
	}

}
