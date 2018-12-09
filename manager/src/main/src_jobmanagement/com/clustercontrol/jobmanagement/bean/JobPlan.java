/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * ジョブ[スケジュール予定]のDTOです
 * @version 4.1.0
 * @since 4.1.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobPlan implements Serializable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -1488596202337667156L;

	/** マネージャ名 */
	private String managerName = null;
	/** 日時 */
	private Long date = null;
	/** 実行契機ID */
	private String jobKickId = null;
	/** 実行契機名 */
	private String jobKickName = null;
	/** ジョブユニットID */
	private String jobunitId = null;
	/** ジョブID */
	private String jobId = null;
	/** ジョブ名*/
	private String jobName = null;

	/** マネージャ名 */
	public String getManagerName() {
		return managerName;
	}
	/** マネージャ名 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
	/** 日時 */
	public Long getDate() {
		return date;
	}
	/** 日時 */
	public void setDate(Long date) {
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
	@Override
	public String toString() {
		return "JobPlan ["
				+ "date=" + date
				+ ", jobKickId=" + jobKickId
				+ ", jobKickName=" + jobKickName
				+ ", jobunitId=" + jobunitId
				+ ", jobId=" + jobId
				+ ", jobName=" + jobName + "]";
	}
}
