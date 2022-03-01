/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class ForwardBinaryResultRequest extends AgentRequestDto {

	private List<AgtBinaryResultDTORequest> resultList;
	private AgentInfoRequest agentInfo;

	public ForwardBinaryResultRequest() {
	}

	public List<AgtBinaryResultDTORequest> getResultList() {
		return resultList;
	}

	public void setResultList(List<AgtBinaryResultDTORequest> resultList) {
		this.resultList = resultList;
	}

	public AgentInfoRequest getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfoRequest agentInfo) {
		this.agentInfo = agentInfo;
	}

}
