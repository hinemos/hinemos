/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

import java.util.ArrayList;
import java.util.List;

public class GetJobLastRunTimeListResponse {

	public GetJobLastRunTimeListResponse() {
	}

	private List<GetJobLastRunTimeResponse> jobLastRunTimeList = new ArrayList<GetJobLastRunTimeResponse>();

	public List<GetJobLastRunTimeResponse> getJobLastRunTimeList() {
		return jobLastRunTimeList;
	}
	
	public void setJobLastRunTimeList(List<GetJobLastRunTimeResponse> jobLastRunTimeArray) {
		this.jobLastRunTimeList = jobLastRunTimeArray;
	}
}
