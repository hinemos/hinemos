/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.approval.util;

import org.openapitools.client.model.JobApprovalInfoResponse;

public class JobApprovalInfoWrapper extends JobApprovalInfoResponse {

	private String  managerName;

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	// findbugs対応 equalsの内容を明示化
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	// findbugs対応 hashCodeの内容を明示化
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
