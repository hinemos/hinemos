/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.agent.dto;

import com.clustercontrol.rest.endpoint.agent.dto.AgentInfoRequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgentRequestDto;
import com.clustercontrol.rest.endpoint.agent.dto.AgtOutputBasicInfoRequest;

public class SendSdmlMessageRequest extends AgentRequestDto {
	private AgtOutputBasicInfoRequest outputBasicInfo = null;
	private AgentInfoRequest agentInfo = null;

	public SendSdmlMessageRequest() {
	}

	public AgtOutputBasicInfoRequest getOutputBasicInfo() {
		return outputBasicInfo;
	}

	public void setOutputBasicInfo(AgtOutputBasicInfoRequest outputBasicInfo) {
		this.outputBasicInfo = outputBasicInfo;
	}

	public AgentInfoRequest getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfoRequest agentInfo) {
		this.agentInfo = agentInfo;
	}
}
