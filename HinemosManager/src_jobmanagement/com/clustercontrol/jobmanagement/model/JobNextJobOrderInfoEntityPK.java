/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The primary key class for the cc_job_info database table.
 * 
 */
@Embeddable
public class JobNextJobOrderInfoEntityPK implements Serializable  {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String nextJobId;

	public JobNextJobOrderInfoEntityPK() {
	}
	
	public JobNextJobOrderInfoEntityPK(String sessionId, String jobunitId, String jobId, String nextJobId) {
		this.setSessionId(sessionId);
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setNextJobId(nextJobId);
	}

	@Column(name="session_id")
	public String getSessionId() {
		return this.sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Column(name="jobunit_id")
	public String getJobunitId() {
		return this.jobunitId;
	}
	public void setJobunitId(String jobunitid) {
		this.jobunitId = jobunitid;
	}

	@Column(name="job_id")
	public String getJobId() {
		return this.jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Column(name="next_job_id")
	public String getNextJobId() {
		return this.nextJobId;
	}
	public void setNextJobId(String nextJobId) {
		this.nextJobId = nextJobId;
	}
}
