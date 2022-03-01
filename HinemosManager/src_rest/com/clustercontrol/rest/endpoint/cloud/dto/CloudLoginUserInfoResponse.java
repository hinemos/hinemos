/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

public class CloudLoginUserInfoResponse {
	private CloudLoginUserEntityResponse entity;

	public CloudLoginUserInfoResponse() {
	}

	public CloudLoginUserEntityResponse getEntity() {
		return entity;
	}

	public void setEntity(CloudLoginUserEntityResponse entity) {
		this.entity = entity;
	}
}
