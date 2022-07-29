/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

public class RegisterNodeConfigInfoRequest extends AgentRequestDto {

	private Long registerDatetime;
	private AgtNodeInfoRequest nodeInfo;

	public RegisterNodeConfigInfoRequest() {
	}

	public Long getRegisterDatetime() {
		return registerDatetime;
	}

	public void setRegisterDatetime(Long registerDatetime) {
		this.registerDatetime = registerDatetime;
	}

	public AgtNodeInfoRequest getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(AgtNodeInfoRequest nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

}
