/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class JobRpaRunConditionEntityPK implements Serializable {
	private static final long serialVersionUID = 1L;
	/** セッションID */
	private String sessionId;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** ファシリティID */
	private String facilityId;

	public JobRpaRunConditionEntityPK() {
	}

	public JobRpaRunConditionEntityPK(String sessionId, String jobunitId, String jobId, String facilityId) {
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.facilityId = facilityId;
	}

	/**
	 * @return セッションIDを返します。
	 */
	@Column(name = "session_id")
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            セッションIDを設定します。
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return ジョブユニットIDを返します。
	 */
	@Column(name = "jobunit_id")
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
	@Column(name = "job_id")
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
	 * @return ファシリティIDを返します。
	 */
	@Column(name = "facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	/**
	 * @param facilityId
	 *            ファシリティIDを設定します。
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
}
