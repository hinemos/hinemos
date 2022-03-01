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
 * The primary key class for the cc_job_wait_group_mst database table.
 * 
 */
@Embeddable
public class JobWaitGroupMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String jobunitId;
	private String jobId;
	private Integer orderNo;

	public JobWaitGroupMstEntityPK() {
	}

	public JobWaitGroupMstEntityPK(String jobunitId, String jobId, Integer orderNo) {
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setOrderNo(orderNo);
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
		if (!(other instanceof JobWaitGroupMstEntityPK)) {
			return false;
		}
		JobWaitGroupMstEntityPK castOther = (JobWaitGroupMstEntityPK)other;
		return
				this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.orderNo.equals(castOther.orderNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.orderNo.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"jobunitId",
				"jobId",
				"orderNo"
		};
		String[] values = {
				this.jobunitId,
				this.jobId,
				this.orderNo.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}