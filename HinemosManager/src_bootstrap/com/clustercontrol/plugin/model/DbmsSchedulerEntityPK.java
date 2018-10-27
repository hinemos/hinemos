/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_dbms_scheduler_Job_trigger database table.
 * 
 */
@Embeddable
public class DbmsSchedulerEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobId		= null;
	private String jobGroup		= null;

	@Deprecated
	public DbmsSchedulerEntityPK() {
	}

	public DbmsSchedulerEntityPK(String jobId, String jobGroup) {
		this.setJobId(jobId);
		this.setJobGroup(jobGroup);
	}

	@Column(name="job_id")
	public String getJobId() {
		return this.jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Column(name="job_group")
	public String getJobGroup() {
		return this.jobGroup;
	}

	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DbmsSchedulerEntityPK)) {
			return false;
		}
		DbmsSchedulerEntityPK castOther = (DbmsSchedulerEntityPK)other;
		return
				this.jobId.equals(castOther.jobId)
				&& this.jobGroup.equals(castOther.jobGroup);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.jobGroup.hashCode();

		return hash;
	}
}