/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

public class GetJobLastRunTimeResponse {

	public GetJobLastRunTimeResponse() {
	}

	private String triggerInfo;
	private String jobId;
	private String latestStartDate;
	private String latestEndDate;

	public String getTriggerInfo() {
		return triggerInfo;
	}
	
	public void setTriggerInfo(String triggerInfo) {
		this.triggerInfo = triggerInfo;
	}
	
	public String getJobId() {
		return jobId;
	}
	
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	public String getLatestStartDate() {
		return latestStartDate;
	}
	
	public void setLatestStartDate(String latestStartDate) {
		this.latestStartDate = latestStartDate;
	}

	public String getLatestEndDate() {
		return latestEndDate;
	}
	
	public void setLatestEndDate(String latestEndDate) {
		this.latestEndDate = latestEndDate;
	}
}
