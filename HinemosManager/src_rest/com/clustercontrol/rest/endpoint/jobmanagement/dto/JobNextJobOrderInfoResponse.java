/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class JobNextJobOrderInfoResponse {
	/** ジョブユニットID */
	private String jobunit_id;

	/** 先行ジョブID */
	private String job_id;

	/** 後続ジョブID */
	private String next_job_id;

	public JobNextJobOrderInfoResponse() {
	}


	public String getJobunit_id() {
		return jobunit_id;
	}

	public void setJobunit_id(String jobunit_id) {
		this.jobunit_id = jobunit_id;
	}

	public String getJob_id() {
		return job_id;
	}

	public void setJob_id(String job_id) {
		this.job_id = job_id;
	}

	public String getNext_job_id() {
		return next_job_id;
	}

	public void setNext_job_id(String next_job_id) {
		this.next_job_id = next_job_id;
	}


}
