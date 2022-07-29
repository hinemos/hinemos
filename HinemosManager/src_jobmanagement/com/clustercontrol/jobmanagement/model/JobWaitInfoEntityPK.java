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
 * The primary key class for the cc_job_wait_info database table.
 * 
 */
@Embeddable
public class JobWaitInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private Integer orderNo;
	private Integer targetJobType;
	private String targetJobunitId;
	private String targetJobId;
	private Integer targetInt1;
	private Integer targetInt2;
	private String targetStr1;
	private String targetStr2;
	private Long targetLong;


	public JobWaitInfoEntityPK() {
	}

	public JobWaitInfoEntityPK(String sessionId,
			String jobunitId,
			String jobId,
			Integer orderNo,
			Integer targetJobType,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			Integer targetInt2,
			String targetStr1,
			String targetStr2,
			Long targetLong) {
		this.setSessionId(sessionId);
		this.setJobunitId(jobunitId);
		this.setJobId(jobId);
		this.setOrderNo(orderNo);
		this.setTargetJobType(targetJobType);
		this.setTargetJobunitId(targetJobunitId);
		this.setTargetJobId(targetJobId);
		this.setTargetInt1(targetInt1);
		this.setTargetInt2(targetInt2);
		this.setTargetStr1(targetStr1);
		this.setTargetStr2(targetStr2);
		this.setTargetLong(targetLong);
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

	@Column(name="order_no")
	public Integer getOrderNo() {
		return this.orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Column(name="target_job_type")
	public Integer getTargetJobType() {
		return this.targetJobType;
	}
	public void setTargetJobType(Integer targetJobType) {
		this.targetJobType = targetJobType;
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

	@Column(name="target_int1")
	public Integer getTargetInt1() {
		return this.targetInt1;
	}
	public void setTargetInt1(Integer targetInt1) {
		this.targetInt1 = targetInt1;
	}

	@Column(name="target_int2")
	public Integer getTargetInt2() {
		return this.targetInt2;
	}
	public void setTargetInt2(Integer targetInt2) {
		this.targetInt2 = targetInt2;
	}

	@Column(name="target_str1")
	public String getTargetStr1() {
		return this.targetStr1;
	}
	public void setTargetStr1(String targetStr1) {
		this.targetStr1 = targetStr1;
	}

	@Column(name="target_str2")
	public String getTargetStr2() {
		return this.targetStr2;
	}
	public void setTargetStr2(String targetStr2) {
		this.targetStr2 = targetStr2;
	}

	@Column(name="target_long")
	public Long getTargetLong() {
		return this.targetLong;
	}
	public void setTargetLong(Long targetLong) {
		this.targetLong = targetLong;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobWaitInfoEntityPK)) {
			return false;
		}
		JobWaitInfoEntityPK castOther = (JobWaitInfoEntityPK)other;
		return
				this.sessionId.equals(castOther.sessionId)
				&& this.jobunitId.equals(castOther.jobunitId)
				&& this.jobId.equals(castOther.jobId)
				&& this.orderNo.equals(castOther.orderNo)
				&& this.targetJobType.equals(castOther.targetJobType)
				&& this.targetJobunitId.equals(castOther.targetJobunitId)
				&& this.targetJobId.equals(castOther.targetJobId)
				&& this.targetInt1.equals(castOther.targetInt1)
				&& this.targetInt2.equals(castOther.targetInt2)
				&& this.targetStr1.equals(castOther.targetStr1)
				&& this.targetStr2.equals(castOther.targetStr2)
				&& this.targetLong.equals(castOther.targetLong);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.sessionId.hashCode();
		hash = hash * prime + this.jobunitId.hashCode();
		hash = hash * prime + this.jobId.hashCode();
		hash = hash * prime + this.orderNo.hashCode();
		hash = hash * prime + this.targetJobType.hashCode();
		hash = hash * prime + this.targetJobunitId.hashCode();
		hash = hash * prime + this.targetJobId.hashCode();
		hash = hash * prime + this.targetInt1.hashCode();
		hash = hash * prime + this.targetInt2.hashCode();
		hash = hash * prime + this.targetStr1.hashCode();
		hash = hash * prime + this.targetStr2.hashCode();
		hash = hash * prime + this.targetLong.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "JobWaitInfoEntityPK ["
				+ "sessionId=" + sessionId
				+ ",jobunitId=" + jobunitId
				+ ", jobId=" + jobId
				+ ", orderNo=" + orderNo
				+ ", targetJobType=" + targetJobType
				+ ", targetJobunitId=" + targetJobunitId
				+ ", targetJobId=" + targetJobId
				+ ", targetInt1=" + targetInt1
				+ ", targetInt2=" + targetInt2
				+ ", targetStr1=" + targetStr1
				+ ", targetStr2=" + targetStr2
				+ ", targetLong=" + targetLong + "]";
	}
}