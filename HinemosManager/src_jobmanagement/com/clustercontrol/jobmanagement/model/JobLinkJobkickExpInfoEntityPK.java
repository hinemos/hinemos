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
 * The primary key class for the cc_job_link_jobkick_exp_info database table.
 * 
 */
@Embeddable
public class JobLinkJobkickExpInfoEntityPK implements Serializable {

	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String jobkickId;
	private String key;

	public JobLinkJobkickExpInfoEntityPK() {
	}

	public JobLinkJobkickExpInfoEntityPK(
			String jobkickId,
			String key) {
		this.setJobkickId(jobkickId);
		this.setKey(key);
	}

	@Column(name="jobkick_id")
	public String getJobkickId() {
		return this.jobkickId;
	}
	public void setJobkickId(String jobkickId) {
		this.jobkickId = jobkickId;
	}

	@Column(name="key")
	public String getKey() {
		return this.key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobLinkJobkickExpInfoEntityPK)) {
			return false;
		}
		JobLinkJobkickExpInfoEntityPK castOther = (JobLinkJobkickExpInfoEntityPK)other;
		return
				this.jobkickId.equals(castOther.jobkickId)
				&& this.key.equals(castOther.key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobkickId.hashCode();
		hash = hash * prime + this.key.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		return "JobLinkJobExpMstEntityPK ["
				+ "jobkickId=" + jobkickId
				+ ", key=" + key + "]";
	}
}