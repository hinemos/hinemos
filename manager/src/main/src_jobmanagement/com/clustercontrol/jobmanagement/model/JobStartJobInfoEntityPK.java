/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_job_start_job_info database table.
 * 
 */
@Embeddable
public class JobStartJobInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String targetJobunitId;
	private String targetJobId;
	private Integer targetJobType;
	private Integer targetJobEndValue;

	public JobStartJobInfoEntityPK() {
	}

	public JobStartJobInfoEntityPK(String sessionId,
			String jobunitId,
			String jobId,
			String targetJobunitId,
			String targetJobId,
			Integer targetJobType,
			Integer targetJobEndValue) {
		this.setSessionId(sessionId);
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setTargetJobunitId(targetJobunitId);
		this.setTargetJobId(targetJobId);
		this.setTargetJobType(targetJobType);
		this.setTargetJobEndValue(targetJobEndValue);
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

	@Column(name="target_jobunit_id")
	public String getTargetJobunitId() {
		return this.targetJobunitId;
	}
	public void setTargetJobunitId(String targetJobunitId) {
		this.targetJobunitId = targetJobunitId;
	}

	@Column(name="target_job_id")
	public String getTargetJobId() {
		return this.targetJobId;
	}
	public void setTargetJobId(String targetJobId) {
		this.targetJobId = targetJobId;
	}

	@Column(name="target_job_type")
	public Integer getTargetJobType() {
		return this.targetJobType;
	}
	public void setTargetJobType(Integer targetJobType) {
		this.targetJobType = targetJobType;
	}

	@Column(name="target_job_end_value")
	public Integer getTargetJobEndValue() {
		return this.targetJobEndValue;
	}
	public void setTargetJobEndValue(Integer targetJobEndValue) {
		this.targetJobEndValue = targetJobEndValue;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobStartJobInfoEntityPK)) {
			return false;
		}
		JobStartJobInfoEntityPK castOther = (JobStartJobInfoEntityPK)other;
		return
				this.sessionId.equals(castOther.sessionId)
				&& this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.targetJobunitId.equals(castOther.targetJobunitId)
				&& this.targetJobId.equals(castOther.targetJobId)
				&& this.targetJobType.equals(castOther.targetJobType)
				&& this.targetJobEndValue.equals(castOther.targetJobEndValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.sessionId.hashCode();
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.targetJobunitId.hashCode();
		hash = hash * prime + this.targetJobId.hashCode();
		hash = hash * prime + this.targetJobType.hashCode();
		hash = hash * prime + this.targetJobEndValue.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"sessionId",
				"jobunitId",
				"jobId",
				"targetJobunitId",
				"targetJobId",
				"targetJobType",
				"targetJobEndValue"
		};
		String[] values = {
				this.sessionId,
				this.jobunitId,
				this.jobId,
				this.targetJobunitId,
				this.targetJobId,
				this.targetJobType.toString(),
				this.targetJobEndValue.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}