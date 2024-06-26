/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.filtersetting.bean.JobHistoryFilterConditionInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTriggerTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StatusEnum;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertAssertion(to = JobHistoryFilterConditionInfo.class)
public class JobHistoryFilterConditionRequest implements RequestDto {

	@RestItemName(MessageConstant.FILTER_COND_DESCRIPTION)
	@RestValidateString(maxLen = FilterSettingConstant.CONDITION_DESC_LEN_MAX)
	private String description;

	@RestItemName(MessageConstant.FILTER_COND_NEGATIVE)
	private Boolean negative;

	@RestItemName(MessageConstant.JOB_START_DATE_FROM)
	@RestBeanConvertDatetime
	private String startDateFrom;

	@RestItemName(MessageConstant.JOB_START_DATE_TO)
	@RestBeanConvertDatetime
	private String startDateTo;

	@RestItemName(MessageConstant.JOB_END_DATE_FROM)
	@RestBeanConvertDatetime
	private String endDateFrom;

	@RestItemName(MessageConstant.JOB_END_DATE_TO)
	@RestBeanConvertDatetime
	private String endDateTo;

	@RestItemName(MessageConstant.SESSION_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String sessionId;

	@RestItemName(MessageConstant.JOB_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String jobId;

	@RestItemName(MessageConstant.JOB_STATUS)
	@RestBeanConvertEnum
	private StatusEnum status;

	@RestItemName(MessageConstant.JOB_END_STATUS)
	@RestBeanConvertEnum
	private EndStatusEnum endStatus;

	@RestItemName(MessageConstant.JOB_TRIGGER_TYPE)
	@RestBeanConvertEnum
	private JobTriggerTypeEnum triggerType;

	@RestItemName(MessageConstant.JOB_TRIGGER_INFO)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String triggerInfo;

	@RestItemName(MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String ownerRoleId;

	@Override
	public void correlationCheck() throws InvalidSetting {
		// AND結合数
		Utils.validateLAndConjunction(MessageConstant.JOB_ID, jobId);
		Utils.validateLAndConjunction(MessageConstant.JOB_TRIGGER_INFO, triggerInfo);
		Utils.validateLAndConjunction(MessageConstant.OWNER_ROLE_ID, ownerRoleId);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getNegative() {
		return negative;
	}

	public void setNegative(Boolean negative) {
		this.negative = negative;
	}

	public String getStartDateFrom() {
		return startDateFrom;
	}

	public void setStartDateFrom(String startDateFrom) {
		this.startDateFrom = startDateFrom;
	}

	public String getStartDateTo() {
		return startDateTo;
	}

	public void setStartDateTo(String startDateTo) {
		this.startDateTo = startDateTo;
	}

	public String getEndDateFrom() {
		return endDateFrom;
	}

	public void setEndDateFrom(String endDateFrom) {
		this.endDateFrom = endDateFrom;
	}

	public String getEndDateTo() {
		return endDateTo;
	}

	public void setEndDateTo(String endDateTo) {
		this.endDateTo = endDateTo;
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

	public JobTriggerTypeEnum getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(JobTriggerTypeEnum triggerType) {
		this.triggerType = triggerType;
	}

	public String getTriggerInfo() {
		return triggerInfo;
	}

	public void setTriggerInfo(String triggerInfo) {
		this.triggerInfo = triggerInfo;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
