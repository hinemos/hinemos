/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import javax.xml.bind.annotation.XmlType;

/**
 * ジョブ履歴のためのクラス。
 * session beanからこのクラスが渡される。
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobHistory implements java.io.Serializable {

	private static final long serialVersionUID = 5922208688177525699L;
	private Integer status = null;
	private Integer endStatus = null;
	private Integer endValue = null;
	private String sessionId = null;
	private String jobId = null;
	private String jobunitId = null;
	private String jobName = null;
	private Integer jobType = null;
	private String facilityId = null;
	private String scope = null;
	private String ownerRoleId = null;
	private Long scheduleDate = null;
	private Long startDate = null;
	private Long endDate = null;
	private Integer jobTriggerType = null;
	private String triggerInfo = null;
	private String managerName = null;

	public JobHistory() {};
	public JobHistory(Integer status, Integer endStatus, Integer endValue,
			String sessionId, String jobId, String jobunitId, String jobName,
			Integer jobType, String facilityId, String scope,
			String ownerRoleId,
			Long scheduleDate, Long startDate, Long endDate,
			Integer jobTriggerType, String triggerInfo, String managerName) {
		super();
		this.status = status;
		this.endStatus = endStatus;
		this.endValue = endValue;
		this.sessionId = sessionId;
		this.jobId = jobId;
		this.jobunitId = jobunitId;
		this.jobName = jobName;
		this.jobType = jobType;
		this.facilityId = facilityId;
		this.scope = scope;
		this.ownerRoleId = ownerRoleId;
		this.scheduleDate = scheduleDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.jobTriggerType = jobTriggerType;
		this.triggerInfo = triggerInfo;
		this.managerName = managerName;
	}

	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getEndStatus() {
		return endStatus;
	}
	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}
	public Integer getEndValue() {
		return endValue;
	}
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobunitId() {
		return jobunitId;
	}
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public Integer getJobType() {
		return jobType;
	}
	public void setJobType(Integer jobType) {
		this.jobType = jobType;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public Long getScheduleDate() {
		return scheduleDate;
	}
	public void setScheduleDate(Long scheduleDate) {
		this.scheduleDate = scheduleDate;
	}
	public Long getStartDate() {
		return startDate;
	}
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}
	public Long getEndDate() {
		return endDate;
	}
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
	public Integer getJobTriggerType() {
		return jobTriggerType;
	}
	public void setJobTriggerType(Integer jobTriggerType) {
		this.jobTriggerType = jobTriggerType;
	}
	public String getTriggerInfo() {
		return triggerInfo;
	}
	public void setTriggerInfo(String triggerInfo) {
		this.triggerInfo = triggerInfo;
	}
	public String getManagerName() {
		return managerName;
	}
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
}
