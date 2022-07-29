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
 * The primary key class for the cc_job_runtime_param database table.
 * 
 */
@Embeddable
public class JobRuntimeParamEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobkickId;
	private String paramId;

	public JobRuntimeParamEntityPK() {
	}

	public JobRuntimeParamEntityPK(String jobkickId, String paramId) {
		this.setJobkickId(jobkickId);
		this.setParamId(paramId);
	}

	@Column(name="jobkick_id")
	public String getJobkickId() {
		return this.jobkickId;
	}
	public void setJobkickId(String jobkickId) {
		this.jobkickId = jobkickId;
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
		if (!(other instanceof JobRuntimeParamEntityPK)) {
			return false;
		}
		JobRuntimeParamEntityPK castOther = (JobRuntimeParamEntityPK)other;
		return
				this.jobkickId.equals(castOther.jobkickId)
				&& this.paramId.equals(castOther.paramId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobkickId.hashCode();
		hash = hash * prime + this.paramId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"jobkickId",
				"paramId"
		};
		String[] values = {
				this.jobkickId,
				this.paramId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}