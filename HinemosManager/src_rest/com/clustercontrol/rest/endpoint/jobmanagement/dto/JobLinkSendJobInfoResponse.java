/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class JobLinkSendJobInfoResponse extends AbstractJobResponse {

	/** ジョブ連携送信ジョブ情報 */
	private JobLinkSendInfoResponse jobLinkSend;

	public JobLinkSendJobInfoResponse() {
	}

	public JobLinkSendInfoResponse getJobLinkSend() {
		return jobLinkSend;
	}

	public void setJobLinkSend(JobLinkSendInfoResponse jobLinkSend) {
		this.jobLinkSend = jobLinkSend;
	}
}
