/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_job_link_inherit_info database table.
 * 
 */
@Embeddable
public class JobLinkInheritInfoEntityPK implements Serializable {

	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String paramId;

	public JobLinkInheritInfoEntityPK() {
	}

	public JobLinkInheritInfoEntityPK(
			String sessionId,
			String jobunitId,
			String jobId,
			String paramId) {
		this.setSessionId(sessionId);
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setParamId(paramId);
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
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	@Column(name="job_id")
	public String getJobId() {
		return this.jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Column(name="param_id")
	public String getParamId() {
		return this.paramId;
	}
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobLinkInheritInfoEntityPK)) {
			return false;
		}
		JobLinkInheritInfoEntityPK castOther = (JobLinkInheritInfoEntityPK)other;
		return
				this.sessionId.equals(castOther.sessionId)
				&& this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.paramId.equals(castOther.paramId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.sessionId.hashCode();
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.paramId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		return "JobLinkJobExpInfoEntityPK ["
				+ "sessionId=" + sessionId
				+ "jobunitId=" + jobunitId
				+ ", jobId=" + jobId
				+ ", paramId=" + paramId + "]";
	}
}