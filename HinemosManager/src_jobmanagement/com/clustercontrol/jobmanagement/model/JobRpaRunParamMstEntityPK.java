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
 * The primary key class for the cc_job_rpa_request_param_mst database table.
 * 
 */
@Embeddable
public class JobRpaRunParamMstEntityPK implements Serializable {
	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** パラメータID */
	private Integer paramId;

	public JobRpaRunParamMstEntityPK() {
	}

	public JobRpaRunParamMstEntityPK(String jobunitId, String jobId, Integer paramId) {
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.paramId = paramId;
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
	 * @return パラメータIDを返します。
	 */
	@Column(name = "param_id")
	public Integer getParamId() {
		return paramId;
	}

	/**
	 * @param paramId
	 *            パラメータIDを設定します。
	 */
	public void setParamId(Integer paramId) {
		this.paramId = paramId;
	}
}
