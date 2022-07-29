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

import jakarta.persistence.*;

/**
 * The primary key class for the cc_job_env_variable_mst database table.
 * 
 */
@Embeddable
public class JobEnvVariableMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobunitId;
	private String jobId;
	private String envVariableId;

	public JobEnvVariableMstEntityPK() {
	}

	public JobEnvVariableMstEntityPK(String jobunitId, String jobId, String envVariableId) {
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setEnvVariableId(envVariableId);
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

	@Column(name="env_variable_id")
	public String getEnvVariableId() {
		return this.envVariableId;
	}
	public void setEnvVariableId(String envVariableId) {
		this.envVariableId = envVariableId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobEnvVariableMstEntityPK)) {
			return false;
		}
		JobEnvVariableMstEntityPK castOther = (JobEnvVariableMstEntityPK)other;
		return
				this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.envVariableId.equals(castOther.envVariableId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.envVariableId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"jobunitId",
				"jobId",
				"envVariableId"
		};
		String[] values = {
				this.jobunitId,
				this.jobId,
				this.envVariableId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}