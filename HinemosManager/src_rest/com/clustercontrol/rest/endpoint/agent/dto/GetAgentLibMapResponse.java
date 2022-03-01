/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.Map;

public class GetAgentLibMapResponse {

	private Map<String, String> md5Map;

	public GetAgentLibMapResponse() {
	}

	public Map<String, String> getMd5Map() {
		return md5Map;
	}

	public void setMd5Map(Map<String, String> md5Map) {
		this.md5Map = md5Map;
	}

}
