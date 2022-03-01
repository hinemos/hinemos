/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class GetJobFullListRequest implements RequestDto {

	private  List<JobInfoRequestP1> jobList;

	
	public GetJobFullListRequest() {
	}


	public List<JobInfoRequestP1> getJobList() {
		return jobList;
	}


	public void setJobList(List<JobInfoRequestP1> jobList) {
		this.jobList = jobList;
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
