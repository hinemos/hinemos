/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

public class SetFileCheckResultRequest extends AgentRequestDto {

	private AgtJobFileCheckRequest jobFileCheckRequest;

	private AgentInfoRequest agentInfo;

	public SetFileCheckResultRequest() {
	}

	public AgtJobFileCheckRequest getJobFileCheckRequest() {
		return jobFileCheckRequest;
	}

	public void setJobFileCheckRequest(AgtJobFileCheckRequest jobFileCheckRequest) {
		this.jobFileCheckRequest = jobFileCheckRequest;
	}

	public AgentInfoRequest getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfoRequest agentInfo) {
		this.agentInfo = agentInfo;
	}

}
