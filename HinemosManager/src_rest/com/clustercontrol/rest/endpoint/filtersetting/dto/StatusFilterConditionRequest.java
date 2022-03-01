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
import com.clustercontrol.filtersetting.bean.StatusFilterConditionInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertAssertion(to = StatusFilterConditionInfo.class)
public class StatusFilterConditionRequest implements RequestDto {

	@RestItemName(MessageConstant.FILTER_COND_DESCRIPTION)
	@RestValidateString(maxLen = FilterSettingConstant.CONDITION_DESC_LEN_MAX)
	private String description;

	@RestItemName(MessageConstant.FILTER_COND_NEGATIVE)
	private Boolean negative;

	@RestItemName(MessageConstant.PRIORITY_CRITICAL)
	private Boolean priorityCritical;

	@RestItemName(MessageConstant.PRIORITY_WARNING)
	private Boolean priorityWarning;

	@RestItemName(MessageConstant.PRIORITY_INFO)
	private Boolean priorityInfo;

	@RestItemName(MessageConstant.PRIORITY_UNKNOWN)
	private Boolean priorityUnknown;

	@RestItemName(MessageConstant.UPDATE_DATE_FROM)
	@RestBeanConvertDatetime
	private String outputDateFrom;

	@RestItemName(MessageConstant.UPDATE_DATE_TO)
	@RestBeanConvertDatetime
	private String outputDateTo;

	@RestItemName(MessageConstant.GENERATION_DATE_FROM)
	@RestBeanConvertDatetime
	private String generationDateFrom;

	@RestItemName(MessageConstant.GENERATION_DATE_TO)
	@RestBeanConvertDatetime
	private String generationDateTo;

	@RestItemName(MessageConstant.MONITOR_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorId;

	@RestItemName(MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorDetail;

	@RestItemName(MessageConstant.APPLICATION)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String application;

	@RestItemName(MessageConstant.MESSAGE)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String message;

	@RestItemName(MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String ownerRoleId;

	@Override
	public void correlationCheck() throws InvalidSetting {
		// AND結合数
		Utils.validateLAndConjunction(MessageConstant.MONITOR_ID, monitorId);
		Utils.validateLAndConjunction(MessageConstant.MONITOR_DETAIL_ID, monitorDetail);
		Utils.validateLAndConjunction(MessageConstant.APPLICATION, application);
		Utils.validateLAndConjunction(MessageConstant.MESSAGE, message);
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

	public Boolean getPriorityCritical() {
		return priorityCritical;
	}

	public void setPriorityCritical(Boolean priorityCritical) {
		this.priorityCritical = priorityCritical;
	}

	public Boolean getPriorityWarning() {
		return priorityWarning;
	}

	public void setPriorityWarning(Boolean priorityWarning) {
		this.priorityWarning = priorityWarning;
	}

	public Boolean getPriorityInfo() {
		return priorityInfo;
	}

	public void setPriorityInfo(Boolean priorityInfo) {
		this.priorityInfo = priorityInfo;
	}

	public Boolean getPriorityUnknown() {
		return priorityUnknown;
	}

	public void setPriorityUnknown(Boolean priorityUnknown) {
		this.priorityUnknown = priorityUnknown;
	}

	public String getOutputDateFrom() {
		return outputDateFrom;
	}

	public void setOutputDateFrom(String outputDateFrom) {
		this.outputDateFrom = outputDateFrom;
	}

	public String getOutputDateTo() {
		return outputDateTo;
	}

	public void setOutputDateTo(String outputDateTo) {
		this.outputDateTo = outputDateTo;
	}

	public String getGenerationDateFrom() {
		return generationDateFrom;
	}

	public void setGenerationDateFrom(String generationDateFrom) {
		this.generationDateFrom = generationDateFrom;
	}

	public String getGenerationDateTo() {
		return generationDateTo;
	}

	public void setGenerationDateTo(String generationDateTo) {
		this.generationDateTo = generationDateTo;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetail() {
		return monitorDetail;
	}

	public void setMonitorDetail(String monitorDetail) {
		this.monitorDetail = monitorDetail;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
