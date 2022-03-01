/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class GetJobQueueActivityInfoRequest extends GetJobQueueListSearchRequest implements RequestDto {

	private Integer jobCountFrom;

	private Integer jobCountTo;

	public GetJobQueueActivityInfoRequest(){
	}

	public Integer getJobCountFrom() {
		return jobCountFrom;
	}

	public void setJobCountFrom(Integer jobCountFrom) {
		this.jobCountFrom = jobCountFrom;
	}

	public Integer getJobCountTo() {
		return jobCountTo;
	}

	public void setJobCountTo(Integer jobCountTo) {
		this.jobCountTo = jobCountTo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
