/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class JobLinkRcvJobInfoResponse extends AbstractJobResponse {

	/** ジョブ連携待機ジョブ情報 */
	private JobLinkRcvInfoResponse jobLinkRcv;

	public JobLinkRcvJobInfoResponse() {
	}

	public JobLinkRcvInfoResponse getJobLinkRcv() {
		return jobLinkRcv;
	}

	public void setJobLinkRcv(JobLinkRcvInfoResponse jobLinkRcv) {
		this.jobLinkRcv = jobLinkRcv;
	}
}
