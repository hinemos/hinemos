/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

public class GetPlatformServicesOnlyServiceNameResponse {
	private List<String> platformServices = new ArrayList<>();

	public GetPlatformServicesOnlyServiceNameResponse() {
	}

	public List<String> getPlatformServices() {
		return platformServices;
	}

	public void setPlatformServices(List<String> platformServices) {
		this.platformServices = platformServices;
	}
}
