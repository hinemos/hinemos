/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class FileJobInfoResponse extends AbstractJobResponse {

	/** ジョブファイル転送情報 */
	private JobFileInfoResponse file;

	public FileJobInfoResponse() {
	}


	public JobFileInfoResponse getFile() {
		return file;
	}


	public void setFile(JobFileInfoResponse file) {
		this.file = file;
	}


}
