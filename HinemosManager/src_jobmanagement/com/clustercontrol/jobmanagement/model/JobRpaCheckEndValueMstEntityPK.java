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
 * The primary key class for the cc_job_rpa_response_end_value_mst database table.
 * 
 */
@Embeddable
public class JobRpaCheckEndValueMstEntityPK implements Serializable {
	private static final long serialVersionUID = 1L;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** RPA管理ツール 終了状態ID */
	private Integer endStatusId;

	public JobRpaCheckEndValueMstEntityPK() {
	}

	public JobRpaCheckEndValueMstEntityPK(String jobunitId, String jobId, Integer endStatusId) {
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.endStatusId = endStatusId;
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
	 * @return RPA管理ツールを返します。
	 */
	@Column(name = "end_status_id")
	public Integer getEndStatusId() {
		return endStatusId;
	}

	/**
	 * @param endStatusId
	 *            RPA管理ツールを設定します。
	 */
	public void setEndStatusId(Integer endStatusId) {
		this.endStatusId = endStatusId;
	}
}
