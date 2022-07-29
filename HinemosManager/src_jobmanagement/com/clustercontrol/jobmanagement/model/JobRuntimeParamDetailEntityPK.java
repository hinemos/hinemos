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
 * The primary key class for the cc_job_runtime_param_detail database table.
 * 
 */
@Embeddable
public class JobRuntimeParamDetailEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobkickId;
	private String paramId;
	private Integer orderNo;

	public JobRuntimeParamDetailEntityPK() {
	}

	public JobRuntimeParamDetailEntityPK(String jobkickId, String paramId, Integer orderNo) {
		this.setJobkickId(jobkickId);
		this.setParamId(paramId);
		this.setOrderNo(orderNo);
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

	@Column(name="order_no")
	public Integer getOrderNo() {
		return this.orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobRuntimeParamDetailEntityPK)) {
			return false;
		}
		JobRuntimeParamDetailEntityPK castOther = (JobRuntimeParamDetailEntityPK)other;
		return
				this.jobkickId.equals(castOther.jobkickId)
				&& this.paramId.equals(castOther.paramId)
				&& this.orderNo.equals(castOther.orderNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobkickId.hashCode();
		hash = hash * prime + this.paramId.hashCode();
		hash = hash * prime + this.orderNo.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"jobkickId",
				"paramId",
				"orderNo"
		};
		String[] values = {
				this.jobkickId,
				this.paramId,
				this.orderNo.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}