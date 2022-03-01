/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class JobPlanResponse {

	/** 日時 */
	@RestBeanConvertDatetime
	private String date;
	/** 実行契機ID */
	private String jobKickId;
	/** 実行契機名 */
	private String jobKickName;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** ジョブ名*/
	private String jobName;

	public JobPlanResponse() {
	}

	public String getDate() {
		return date;
	}
	/** 日時 */
	public void setDate(String date) {
		this.date = date;
	}
	/** 実行契機ID */
	public String getJobKickId() {
		return jobKickId;
	}
	/** 実行契機ID */
	public void setJobKickId(String jobKickId) {
		this.jobKickId = jobKickId;
	}
	/** 実行契機名 */
	public String getJobKickName() {
		return jobKickName;
	}
	/** 実行契機名 */
	public void setJobKickName(String jobKickName) {
		this.jobKickName = jobKickName;
	}
	/** ジョブユニットID */
	public String getJobunitId() {
		return jobunitId;
	}
	/** ジョブユニットID */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}
	/** ジョブID */
	public String getJobId() {
		return jobId;
	}
	/** ジョブID */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	/** ジョブ名*/
	public String getJobName() {
		return jobName;
	}
	/** ジョブ名*/
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
}
