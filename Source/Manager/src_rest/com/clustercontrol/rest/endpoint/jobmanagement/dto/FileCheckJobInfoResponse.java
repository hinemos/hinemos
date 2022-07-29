/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class FileCheckJobInfoResponse extends AbstractJobResponse {

	/** ファイルチェックジョブ情報 */
	private JobFileCheckInfoResponse jobFileCheck;

	public FileCheckJobInfoResponse() {
	}

	public JobFileCheckInfoResponse getJobFileCheck() {
		return jobFileCheck;
	}

	public void setJobFileCheck(JobFileCheckInfoResponse jobFileCheck) {
		this.jobFileCheck = jobFileCheck;
	}
}
