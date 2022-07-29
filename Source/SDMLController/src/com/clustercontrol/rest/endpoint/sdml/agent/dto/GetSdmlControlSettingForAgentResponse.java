/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.agent.dto;

import java.util.List;

public class GetSdmlControlSettingForAgentResponse {
	private List<AgtSdmlControlSettingInfoResponse> list;

	public GetSdmlControlSettingForAgentResponse() {
	}

	public List<AgtSdmlControlSettingInfoResponse> getList() {
		return list;
	}

	public void setList(List<AgtSdmlControlSettingInfoResponse> list) {
		this.list = list;
	}
}
