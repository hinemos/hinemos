/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

public class EndpointEntityResponse {
	private String endpointId;
	private String url;
	public EndpointEntityResponse() {
	}
	public String getEndpointId() {
		return endpointId;
	}
	public void setEndpointId(String endpoinId) {
		this.endpointId = endpoinId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
