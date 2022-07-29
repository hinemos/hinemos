/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class JobQueueReferrerViewInfoListItemResponse {

	private String jobunitId;
	private String jobId;
	private String ownerRoleId;
	private JobInfoResponse jobInfoWithOwnerRoleId;

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public JobInfoResponse getJobInfoWithOwnerRoleId() {
		return jobInfoWithOwnerRoleId;
	}

	public void setJobInfoWithOwnerRoleId(JobInfoResponse jobInfoWithOwnerRoleId) {
		this.jobInfoWithOwnerRoleId = jobInfoWithOwnerRoleId;
	}
}
