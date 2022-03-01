/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

public class SetJobStartResponse {

	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String facilityId;
	private Boolean jobRunnable;

	public SetJobStartResponse() {
	}

	/**
	 * 処理対象のジョブセッションIDを返します。
	 */
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 処理対象のジョブユニットIDを返します。
	 */
	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	/**
	 * 処理対象のジョブIDを返します。
	 */
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * 処理対象のファシリティIDを返します。
	 */
	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * 結果としてジョブを実行可能な場合は true、そうでなければ false を返します。
	 */
	public Boolean getJobRunnable() {
		return jobRunnable;
	}

	public void setJobRunnable(Boolean jobRunnable) {
		this.jobRunnable = jobRunnable;
	}

}