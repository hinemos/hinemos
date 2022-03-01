/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

public class GetJobLinkMessageListResponse {
	List<JobLinkMessageResponse> list = null;
	Integer total = null;

	public GetJobLinkMessageListResponse() {
	}

	public List<JobLinkMessageResponse> getList() {
		return list;
	}

	public void setList(List<JobLinkMessageResponse> list) {
		this.list = list;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}
