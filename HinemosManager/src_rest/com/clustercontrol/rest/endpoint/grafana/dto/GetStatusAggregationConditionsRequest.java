/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.grafana.dto.Utils;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.util.MessageConstant;

public class GetStatusAggregationConditionsRequest implements RequestDto {

	public GetStatusAggregationConditionsRequest() {
	}

	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	
	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_NEGATIVE)
	private boolean negative;
	
	@RestItemName(value = MessageConstant.PRIORITY_CRITICAL)
	private boolean priorityCritical = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.PRIORITY_WARNING)
	private boolean priorityWarning = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.PRIORITY_INFO)
	private boolean priorityInfo = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.PRIORITY_UNKNOWN)
	private boolean priorityUnknown = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.GENERATION_DATE_FROM)
	private String generationDateFrom;
	
	@RestItemName(value = MessageConstant.GENERATION_DATE_TO)
	private String generationDateTo;
	
	@RestItemName(value = MessageConstant.OUTPUT_DATE_FROM)
	private String outputDateFrom;
	
	@RestItemName(value = MessageConstant.OUTPUT_DATE_TO)
	private String outputDateTo;
	
	@RestItemName(value = MessageConstant.MONITOR_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorId;
	
	@RestItemName(value = MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorDetail;
	
	@RestItemName(value = MessageConstant.APPLICATION)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String application;
	
	@RestItemName(value = MessageConstant.MESSAGE)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String message;
		
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String ownerRoleId;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	public boolean isPriorityCritical() {
		return priorityCritical;
	}

	public void setPriorityCritical(boolean priorityCritical) {
		this.priorityCritical = priorityCritical;
	}

	public boolean isPriorityWarning() {
		return priorityWarning;
	}

	public void setPriorityWarning(boolean priorityWarning) {
		this.priorityWarning = priorityWarning;
	}

	public boolean isPriorityInfo() {
		return priorityInfo;
	}

	public void setPriorityInfo(boolean priorityInfo) {
		this.priorityInfo = priorityInfo;
	}

	public boolean isPriorityUnknown() {
		return priorityUnknown;
	}

	public void setPriorityUnknown(boolean priorityUnknown) {
		this.priorityUnknown = priorityUnknown;
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

	/**
	 * true になっている重要度のコードリストを返します。
	 */
	public List<Integer> getPriorityCodes() {
		List<Integer> priorityList = new ArrayList<Integer>();
		if (Boolean.TRUE.equals(priorityInfo)) {
			priorityList.add(PriorityEnum.INFO.getCode());
		}
		if (Boolean.TRUE.equals(priorityWarning)) {
			priorityList.add(PriorityEnum.WARNING.getCode());
		}
		if (Boolean.TRUE.equals(priorityCritical)) {
			priorityList.add(PriorityEnum.CRITICAL.getCode());
		}
		if (Boolean.TRUE.equals(priorityUnknown)) {
			priorityList.add(PriorityEnum.UNKNOWN.getCode());
		}
		return priorityList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// AND結合数
		Utils.validateLAndConjunction(MessageConstant.MONITOR_ID, monitorId);
		Utils.validateLAndConjunction(MessageConstant.MONITOR_DETAIL_ID, monitorDetail);
		Utils.validateLAndConjunction(MessageConstant.APPLICATION, application);
		Utils.validateLAndConjunction(MessageConstant.MESSAGE, message);
		Utils.validateLAndConjunction(MessageConstant.OWNER_ROLE_ID, ownerRoleId);
	}

}
