/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class JobInfoRequestP1 {
	/** 所属ジョブユニットのジョブID */
	private String jobunitId;

	/** ジョブID */
	private String id;

	public JobInfoRequestP1() {
	}


	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
