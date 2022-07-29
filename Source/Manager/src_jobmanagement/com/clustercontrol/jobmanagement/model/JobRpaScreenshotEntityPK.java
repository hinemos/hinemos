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

/**
 * The primary key class for the cc_job_rpa_screenshot database table.
 * 
 */
@Embeddable
public class JobRpaScreenshotEntityPK implements Serializable{
	private static final long serialVersionUID = 1L;
	/** セッションID */
	private String sessionId;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** ファシリティ */
	private String facilityId;
	/** 出力日時 */
	private long outputDate;

	public JobRpaScreenshotEntityPK() {
	}

	public JobRpaScreenshotEntityPK(String sessionId, String jobunitId, String jobId,
			String facilityId, long outputDate) {
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.facilityId = facilityId;
		this.outputDate = outputDate;
	}

	/**
	 * @return セッションIDを返します。
	 */
	@Column(name="session_id")
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
	 * @return ファシリティIDを返します。
	 */
	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * @param facilityId
	 *            ファシリティIDを設定します。
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * @return 出力日時を返します。
	 */
	@Column(name="output_date")
	public long getOutputDate() {
		return outputDate;
	}

	/**
	 * @param outputDate
	 *            出力日時を設定します。
	 */
	public void setOutputDate(long outputDate) {
		this.outputDate = outputDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facilityId == null) ? 0 : facilityId.hashCode());
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		result = prime * result + ((jobunitId == null) ? 0 : jobunitId.hashCode());
		result = prime * result + (int) (outputDate ^ (outputDate >>> 32));
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobRpaScreenshotEntityPK other = (JobRpaScreenshotEntityPK) obj;
		if (facilityId == null) {
			if (other.facilityId != null)
				return false;
		} else if (!facilityId.equals(other.facilityId))
			return false;
		if (jobId == null) {
			if (other.jobId != null)
				return false;
		} else if (!jobId.equals(other.jobId))
			return false;
		if (jobunitId == null) {
			if (other.jobunitId != null)
				return false;
		} else if (!jobunitId.equals(other.jobunitId))
			return false;
		if (outputDate != other.outputDate)
			return false;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		return true;
	}
}
