/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

public class JobQueueSettingViewInfoResponse {

	private List<JobQueueSettingViewInfoListItemResponse> items;

	public JobQueueSettingViewInfoResponse() {
	}

	public List<JobQueueSettingViewInfoListItemResponse> getItems() {
		return items;
	}

	public void setItems(List<JobQueueSettingViewInfoListItemResponse> items) {
		this.items = items;
	}
}
