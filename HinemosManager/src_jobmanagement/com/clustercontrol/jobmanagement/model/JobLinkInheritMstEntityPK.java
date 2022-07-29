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
 * The primary key class for the cc_job_link_inherit_mst database table.
 * 
 */
@Embeddable
public class JobLinkInheritMstEntityPK implements Serializable {

	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String jobunitId;
	private String jobId;
	private String paramId;

	public JobLinkInheritMstEntityPK() {
	}

	public JobLinkInheritMstEntityPK(
			String jobunitId,
			String jobId,
			String paramId) {
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setParamId(paramId);
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
		if (!(other instanceof JobLinkInheritMstEntityPK)) {
			return false;
		}
		JobLinkInheritMstEntityPK castOther = (JobLinkInheritMstEntityPK)other;
		return
				this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.paramId.equals(castOther.paramId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.paramId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		return "JobLinkJobExpMstEntityPK ["
				+ "jobunitId=" + jobunitId
				+ ", jobId=" + jobId
				+ ", paramId=" + paramId + "]";
	}
}