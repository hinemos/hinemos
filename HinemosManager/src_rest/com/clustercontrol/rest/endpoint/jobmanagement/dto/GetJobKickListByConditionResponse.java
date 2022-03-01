/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

public class GetJobKickListByConditionResponse {
	
	private Integer total = 0;			//合計数（size制限が無い場合に取得できた件数）
	private List<JobKickResponse> jobKickList ;

	public GetJobKickListByConditionResponse() {
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<JobKickResponse> getJobKickList() {
		return jobKickList;
	}

	public void setJobKickList(List<JobKickResponse> jobKickList) {
		this.jobKickList = jobKickList;
	}

}
