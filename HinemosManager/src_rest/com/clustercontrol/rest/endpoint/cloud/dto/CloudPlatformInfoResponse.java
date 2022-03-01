/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

public class CloudPlatformInfoResponse {

	private CloudPlatformEntityResponse entity;
	// CloudPlatformはメンバを持っていないので、CloudPlatform#getCloudSpecから取得して個別に変換すること。
	private CloudSpecResponse cloudSpec;

	public CloudPlatformInfoResponse() {
	}

	public CloudPlatformEntityResponse getEntity() {
		return entity;
	}

	public void setEntity(CloudPlatformEntityResponse entity) {
		this.entity = entity;
	}

	public CloudSpecResponse getCloudSpec() {
		return cloudSpec;
	}

	public void setCloudSpec(CloudSpecResponse cloudSpec) {
		this.cloudSpec = cloudSpec;
	}

}
