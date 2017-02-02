package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_job_decision_mst database table.
 * 
 */
@Embeddable
public class JobStartParamMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobunitId;
	private String jobId;
	private String startDecisionValue01;
	private String startDecisionValue02;
	private Integer targetJobType;
	private Integer startDecisionCondition;

	public JobStartParamMstEntityPK() {
	}

	public JobStartParamMstEntityPK(String jobunitId,
			String jobId,
			String startDecisionValue01,
			String startDecisionValue02,
			Integer targetJobType,
			Integer startDecisionCondition) {
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setStartDecisionValue01(startDecisionValue01);
		this.setStartDecisionValue02(startDecisionValue02);
		this.setTargetJobType(targetJobType);
		this.setStartDecisionCondition(startDecisionCondition);
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

	@Column(name="start_decision_value_01")
	public String getStartDecisionValue01() {
		return this.startDecisionValue01;
	}
	public void setStartDecisionValue01(String startDecisionValue01) {
		this.startDecisionValue01 = startDecisionValue01;
	}

	@Column(name="start_decision_value_02")
	public String getStartDecisionValue02() {
		return this.startDecisionValue02;
	}
	public void setStartDecisionValue02(String startDecisionValue02) {
		this.startDecisionValue02 = startDecisionValue02;
	}

	@Column(name="target_job_type")
	public Integer getTargetJobType() {
		return this.targetJobType;
	}
	public void setTargetJobType(Integer targetJobType) {
		this.targetJobType = targetJobType;
	}

	@Column(name="start_decision_condition")
	public Integer getStartDecisionCondition() {
		return this.startDecisionCondition;
	}
	public void setStartDecisionCondition(Integer startDecisionCondition) {
		this.startDecisionCondition = startDecisionCondition;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobStartParamMstEntityPK)) {
			return false;
		}
		JobStartParamMstEntityPK castOther = (JobStartParamMstEntityPK)other;
		return
				this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.startDecisionValue01.equals(castOther.startDecisionValue01)
				&& this.startDecisionValue02.equals(castOther.startDecisionValue02)
				&& this.targetJobType.equals(castOther.targetJobType)
				&& this.startDecisionCondition.equals(castOther.startDecisionCondition);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.startDecisionValue01.hashCode();
		hash = hash * prime + this.startDecisionValue02.hashCode();
		hash = hash * prime + this.targetJobType.hashCode();
		hash = hash * prime + this.startDecisionCondition.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"jobunitId",
				"jobId",
				"startDecisionValue01",
				"startDecisionValue02",
				"targetJobType",
				"startDecisionCondition"
		};
		String[] values = {
				this.jobunitId,
				this.jobId,
				this.startDecisionValue01,
				this.startDecisionValue02,
				this.targetJobType.toString(),
				this.startDecisionCondition.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}