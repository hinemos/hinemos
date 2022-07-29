/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

public class GetJobHistoryListResponse {
	List<JobHistoryResponse> list = null;
	Integer total = null;

	public GetJobHistoryListResponse() {
	}

	public List<JobHistoryResponse> getList() {
		return list;
	}

	public void setList(List<JobHistoryResponse> list) {
		this.list = list;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
	
	
}
