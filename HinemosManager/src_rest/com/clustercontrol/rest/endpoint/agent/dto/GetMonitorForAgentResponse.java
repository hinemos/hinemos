/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class GetMonitorForAgentResponse {
	private List<AgtMonitorInfoResponse> list;

	public GetMonitorForAgentResponse() {
	}

	public List<AgtMonitorInfoResponse> getList() {
		return list;
	}

	public void setList(List<AgtMonitorInfoResponse> list) {
		this.list = list;
	}
}
