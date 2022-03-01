/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.jobmap.dto;

public class CheckPublishResponse {
	public CheckPublishResponse() {
	}

	private Boolean publish;

	public Boolean getPublish() {
		return publish;
	}

	public void setPublish(Boolean publish) {
		this.publish = publish;
	}

	@Override
	public String toString() {
		return "CheckPublishResponse [publish=" + publish + "]";
	}

}
