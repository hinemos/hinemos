/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.Map;

public class SetAgentProfileRequest extends AgentRequestDto {

	private Map<String, String> md5Map;
	private AgentJavaInfoRequest javaInfo;
	private AgentInfoRequest agentInfo;

	public SetAgentProfileRequest() {
	}

	public Map<String, String> getMd5Map() {
		return md5Map;
	}

	public void setMd5Map(Map<String, String> md5Map) {
		this.md5Map = md5Map;
	}

	public AgentJavaInfoRequest getJavaInfo() {
		return javaInfo;
	}

	public void setJavaInfo(AgentJavaInfoRequest javaInfo) {
		this.javaInfo = javaInfo;
	}

	public AgentInfoRequest getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfoRequest agentInfo) {
		this.agentInfo = agentInfo;
	}

}
