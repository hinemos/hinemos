/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

public class JobQueueItemContentResponse {

	private String queueId;
	private String queueName;
	private Integer concurrency;
	private Integer count;
	private Integer activeCount;
	private List<JobQueueContentsViewInfoListItemResponse> items;
	

	public JobQueueItemContentResponse(){
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
	public Integer getConcurrency() {
		return concurrency;
	}
	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getActiveCount() {
		return activeCount;
	}
	public void setActiveCount(Integer activeCount) {
		this.activeCount = activeCount;
	}
	public List<JobQueueContentsViewInfoListItemResponse> getItems() {
		return items;
	}
	public void setItems(List<JobQueueContentsViewInfoListItemResponse> items) {
		this.items = items;
	}
}
