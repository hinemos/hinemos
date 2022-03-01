/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class JobTreeItemResponseP3 {

	/** ジョブ情報 */
	private JobInfoResponse data;

	public JobTreeItemResponseP3() {
	}

	public JobInfoResponse getData() {
		return data;
	}

	public void setData(JobInfoResponse data) {
		this.data = data;
	}


}
