/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class GetNodeInfoListResponse {

	private List<AgtNodeInfoResponse> list;

	public GetNodeInfoListResponse() {
	}

	public List<AgtNodeInfoResponse> getList() {
		return list;
	}

	public void setList(List<AgtNodeInfoResponse> list) {
		this.list = list;
	}

}
