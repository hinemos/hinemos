/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class GetFileCheckResponse {
	private List<AgtJobFileCheckResponse> list;

	public GetFileCheckResponse() {
	}

	public List<AgtJobFileCheckResponse> getList() {
		return list;
	}

	public void setList(List<AgtJobFileCheckResponse> list) {
		this.list = list;
	}
}
