/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

@RestBeanConvertIdClassSet(infoClass = MonitorStringValueInfo.class, idName = "id")
public class MonitorStringValueInfoResponse {
	private Integer orderNo;
	private String message;
	private Boolean caseSensitivityFlg;
	private String description;
	private String pattern;
	@RestBeanConvertEnum
	private PriorityEnum priority;
	private Boolean processType;
	private Boolean validFlg;

	public MonitorStringValueInfoResponse() {
	}

	public Integer getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public PriorityEnum getPriority() {
		return priority;
	}
	public void setPriority(PriorityEnum priority) {
		this.priority = priority;
	}
	public Boolean getProcessType() {
		return processType;
	}
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}
	@Override
	public String toString() {
		return "MonitorStringValueInfo [orderNo=" + orderNo + ", message=" + message
				+ ", caseSensitivityFlg=" + caseSensitivityFlg + ", description=" + description + ", pattern=" + pattern
				+ ", priority=" + priority + ", processType=" + processType + ", validFlg=" + validFlg + "]";
	}

}