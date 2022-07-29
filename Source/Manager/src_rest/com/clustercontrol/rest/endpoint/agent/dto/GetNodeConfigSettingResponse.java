/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class GetNodeConfigSettingResponse {

	private List<AgtNodeConfigSettingResponse> list;

	public GetNodeConfigSettingResponse() {
	}

	public List<AgtNodeConfigSettingResponse> getList() {
		return list;
	}

	public void setList(List<AgtNodeConfigSettingResponse> list) {
		this.list = list;
	}

}
