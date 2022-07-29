/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTriggerTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StatusEnum;

public class JobHistoryResponse {

	@RestBeanConvertEnum
	private StatusEnum status = null;
	@RestBeanConvertEnum
	private EndStatusEnum endStatus = null;
	private Integer endValue = null;
	private String sessionId = null;
	private String jobId = null;
	private String jobunitId = null;
	private String jobName = null;
	@RestBeanConvertEnum
	private JobTypeEnum jobType = null;
	private String facilityId = null;
	@RestPartiallyTransrateTarget
	private String scope = null;
	private String ownerRoleId = null;
	@RestBeanConvertDatetime
	private String scheduleDate = null;
	@RestBeanConvertDatetime
	private String startDate = null;
	@RestBeanConvertDatetime
	private String endDate = null;
	@RestBeanConvertEnum
	private JobTriggerTypeEnum jobTriggerType = null;
	private String triggerInfo = null;

	public JobHistoryResponse() {
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public EndStatusEnum getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(EndStatusEnum endStatus) {
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

	public JobTypeEnum getJobType() {
		return jobType;
	}

	public void setJobType(JobTypeEnum jobType) {
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

	public String getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(String scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public JobTriggerTypeEnum getJobTriggerType() {
		return jobTriggerType;
	}

	public void setJobTriggerType(JobTriggerTypeEnum jobTriggerType) {
		this.jobTriggerType = jobTriggerType;
	}

	public String getTriggerInfo() {
		return triggerInfo;
	}

	public void setTriggerInfo(String triggerInfo) {
		this.triggerInfo = triggerInfo;
	}

}
