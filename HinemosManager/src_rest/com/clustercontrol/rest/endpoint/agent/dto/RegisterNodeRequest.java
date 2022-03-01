/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class RegisterNodeRequest extends AgentRequestDto {

	private String platform;
	private List<AgtNodeNetworkInterfaceInfoRequest> nodeNifList;

	public RegisterNodeRequest() {
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public List<AgtNodeNetworkInterfaceInfoRequest> getNodeNifList() {
		return nodeNifList;
	}

	public void setNodeNifList(List<AgtNodeNetworkInterfaceInfoRequest> nodeNifList) {
		this.nodeNifList = nodeNifList;
	}

}
