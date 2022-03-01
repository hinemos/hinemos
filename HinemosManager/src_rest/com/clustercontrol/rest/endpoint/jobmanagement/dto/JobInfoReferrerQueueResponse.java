/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

public class JobInfoReferrerQueueResponse {

	private String queueId;
	private String queueName;
	private List<JobQueueReferrerViewInfoListItemResponse> items;

	public JobInfoReferrerQueueResponse() {
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public List<JobQueueReferrerViewInfoListItemResponse> getItems() {
		return items;
	}

	public void setItems(List<JobQueueReferrerViewInfoListItemResponse> items) {
		this.items = items;
	}
}
