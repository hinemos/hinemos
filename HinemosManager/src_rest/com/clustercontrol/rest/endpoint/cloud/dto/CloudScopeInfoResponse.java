/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

public class CloudScopeInfoResponse {

	private CloudScopeEntityResponse entity;
	
	public CloudScopeInfoResponse() {
	}

	public CloudScopeEntityResponse getEntity() {
		return entity;
	}

	public void setEntity(CloudScopeEntityResponse entity) {
		this.entity = entity;
	}
}
