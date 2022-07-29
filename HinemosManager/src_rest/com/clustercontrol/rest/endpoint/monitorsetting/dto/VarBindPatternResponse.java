/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.snmptrap.model.VarBindPattern;

@RestBeanConvertIdClassSet(infoClass = VarBindPattern.class, idName = "id")
public class VarBindPatternResponse {
	private Integer orderNo;
	private String description;
	private Boolean processType;
	private String pattern;
	@RestBeanConvertEnum
	private PriorityEnum priority;
	private Boolean caseSensitivityFlg;
	private Boolean validFlg;

	public VarBindPatternResponse() {
	}

	public Integer getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getProcessType() {
		return processType;
	}
	public void setProcessType(Boolean processType) {
		this.processType = processType;
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
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}
	@Override
	public String toString() {
		return "VarBindPattern [orderNo=" + orderNo + ", description=" + description
				+ ", processType=" + processType + ", pattern=" + pattern + ", priority=" + priority
				+ ", caseSensitivityFlg=" + caseSensitivityFlg + ", validFlg=" + validFlg + "]";
	}

}