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
 * The primary key class for the cc_job_rpa_response_end_value_info database
 * table.
 * 
 */
@Embeddable
public class JobRpaCheckEndValueInfoEntityPK implements Serializable {
	private static final long serialVersionUID = 1L;
	/** セッションID */
	private String sessionId;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** RPA管理ツール終了状態ID */
	private Integer endStatusId;

	public JobRpaCheckEndValueInfoEntityPK() {
	}

	public JobRpaCheckEndValueInfoEntityPK(String sessionId, String jobunitId, String jobId, Integer endStatusId) {
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.endStatusId = endStatusId;
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
	 * @return RPA管理ツール終了状態IDを返します。
	 */
	@Column(name = "end_status_id")
	public Integer getEndStatusId() {
		return endStatusId;
	}

	/**
	 * @param endStatusId
	 *            RPA管理ツール終了状態IDを設定します。
	 */
	public void setEndStatusId(Integer endStatusId) {
		this.endStatusId = endStatusId;
	}
}
