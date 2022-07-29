/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;


public class PremakeJobsessionResponse {

	/** 実行契機ID */
	private String jobkickId;

	public String getJobkickId() {
		return jobkickId;
	}

	public void setJobkickId(String jobkickId) {
		this.jobkickId = jobkickId;
	}
}
