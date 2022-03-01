/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class RpaJobInfoResponse extends AbstractJobResponse {

	/** RPAシナリオジョブ情報 */
	private JobRpaInfoRequest rpa;

	public RpaJobInfoResponse() {
	}

	public JobRpaInfoRequest getRpa() {
		return rpa;
	}

	public void setRpa(JobRpaInfoRequest rpa) {
		this.rpa = rpa;
	}

}
