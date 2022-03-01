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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The primary key class for the cc_job_rpa_scenario_option database table.
 * 
 */
@Embeddable
public class JobRpaOptionMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** 順序 */
	private Integer orderNo;
	
	public JobRpaOptionMstEntityPK() {
	}

	public JobRpaOptionMstEntityPK(String jobunitId, String jobId, Integer orderNo) {
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.orderNo = orderNo;
	}

	/**
	 * @return ジョブユニットIDを返します。
	 */
	@Column(name="jobunit_id")
	public String getJobunitId() {
		return jobunitId;
	}

	/**
	 * @param jobunitId
	 *            ジョブユニットIDを設定します。
	 */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	/**
	 * @return ジョブIDを返します。
	 */
	@Column(name="job_id")
	public String getJobId() {
		return jobId;
	}

	/**
	 * @param jobId
	 *            ジョブIDを設定します。
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * @return 順序を返します。
	 */
	@Column(name="order_no")
	public Integer getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo
	 *            順序を設定します。
	 */
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobRpaOptionMstEntityPK)) {
			return false;
		}
		JobRpaOptionMstEntityPK castOther = (JobRpaOptionMstEntityPK)other;
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
				"order"
		};
		String[] values = {
				this.jobunitId,
				this.jobId,
				this.orderNo.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
