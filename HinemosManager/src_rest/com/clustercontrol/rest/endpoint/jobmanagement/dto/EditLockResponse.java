/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class EditLockResponse {
	String jobunitId;
	Integer editSession;

	public EditLockResponse() {
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public Integer getEditSession() {
		return editSession;
	}

	public void setEditSession(Integer editSession) {
		this.editSession = editSession;
	}
	

}
