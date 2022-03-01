/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class GetMonitorCustomResponse {
	private List<AgtCustomMonitorInfoResponse> list;

	public GetMonitorCustomResponse() {
	}

	public List<AgtCustomMonitorInfoResponse> getList() {
		return list;
	}

	public void setList(List<AgtCustomMonitorInfoResponse> list) {
		this.list = list;
	}
}
