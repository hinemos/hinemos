package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_job_env_variable_info database table.
 * 
 */
@Embeddable
public class JobEnvVariableInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String envVariableId;

	public JobEnvVariableInfoEntityPK() {
	}

	public JobEnvVariableInfoEntityPK(String sessionId, String jobunitId, String jobId, String envVariableId) {
		this.setSessionId(sessionId);
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setEnvVariableId(envVariableId);
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
		if (!(other instanceof JobEnvVariableInfoEntityPK)) {
			return false;
		}
		JobEnvVariableInfoEntityPK castOther = (JobEnvVariableInfoEntityPK)other;
		return
				this.sessionId.equals(castOther.sessionId)
				&& this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.envVariableId.equals(castOther.envVariableId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.sessionId.hashCode();
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.envVariableId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"sessionId",
				"jobunitId",
				"jobId",
				"envVariableId"
		};
		String[] values = {
				this.sessionId,
				this.jobunitId,
				this.jobId,
				this.envVariableId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}