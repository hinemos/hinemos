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

public class GetPlatformServiceForLoginUserResponse {
	private List<PlatformServicesResponse> platiformServices = new ArrayList<>();

	public GetPlatformServiceForLoginUserResponse() {
	}

	public List<PlatformServicesResponse> getPlatiformServices() {
		return platiformServices;
	}

	public void setPlatiformServices(List<PlatformServicesResponse> platiformServices) {
		this.platiformServices = platiformServices;
	}
}
