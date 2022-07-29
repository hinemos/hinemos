/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

public class SendMessageRequest extends AgentRequestDto {
	private AgtOutputBasicInfoRequest outputBasicInfo = null;
	private AgentInfoRequest agentInfo = null;

	public SendMessageRequest() {
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
