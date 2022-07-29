/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
 * The primary key class for the cc_job_output_mst database table.
 * 
 */
@Embeddable
public class JobOutputMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobunitId;
	private String jobId;
	private Integer outputType;

	public JobOutputMstEntityPK() {
	}

	public JobOutputMstEntityPK(String jobunitId, String jobId, Integer outputType) {
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setOutputType(outputType);
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

	@Column(name="output_type")
	public Integer getOutputType() {
		return this.outputType;
	}
	public void setOutputType(Integer outputType) {
		this.outputType = outputType;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobOutputMstEntityPK)) {
			return false;
		}
		JobOutputMstEntityPK castOther = (JobOutputMstEntityPK)other;
		return
				this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.outputType.equals(castOther.outputType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.outputType.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"jobunitId",
				"jobId",
				"outputType"
		};
		String[] values = {
				this.jobunitId,
				this.jobId,
				this.outputType.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}