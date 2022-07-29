/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(to = MonitorStringValueInfo.class)
@RestBeanConvertIdClassSet(infoClass = MonitorStringValueInfo.class, idName = "id")
public class AgtMonitorStringValueInfoRequest extends AgentRequestDto {

	// ---- from MonitorStringValueInfo
	private String monitorId;
	private Integer orderNo;
	private String message;
	private Boolean caseSensitivityFlg;
	private String description;
	private String pattern;
	private Integer priority;
	private Boolean processType;
	private Boolean validFlg;
	// private MonitorInfo monitorInfo;

	public AgtMonitorStringValueInfoRequest() {
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
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

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
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

}
