/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

public class JobQueueItemInfoResponse {

	private List<JobQueueActivityViewInfoListItemResponse> items;

	public List<JobQueueActivityViewInfoListItemResponse> getItems() {
		return items;
	}

	public void setItems(List<JobQueueActivityViewInfoListItemResponse> items) {
		this.items = items;
	}
}
