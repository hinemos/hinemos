/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.MonitorNumericTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

@RestBeanConvertIdClassSet(infoClass = MonitorNumericValueInfo.class, idName = "id")
public class MonitorNumericValueInfoRequest implements RequestDto {
	@RestBeanConvertEnum
	private MonitorNumericTypeEnum monitorNumericType;
	@RestBeanConvertEnum
	private PriorityEnum priority;
	private String message;
	private Double thresholdLowerLimit;
	private Double thresholdUpperLimit;
	public MonitorNumericValueInfoRequest() {
	}
	public MonitorNumericTypeEnum getMonitorNumericType() {
		return monitorNumericType;
	}
	public void setMonitorNumericType(MonitorNumericTypeEnum monitorNumericType) {
		this.monitorNumericType = monitorNumericType;
	}
	public PriorityEnum getPriority() {
		return priority;
	}
	public void setPriority(PriorityEnum priority) {
		this.priority = priority;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Double getThresholdLowerLimit() {
		return thresholdLowerLimit;
	}
	public void setThresholdLowerLimit(Double thresholdLowerLimit) {
		this.thresholdLowerLimit = thresholdLowerLimit;
	}
	public Double getThresholdUpperLimit() {
		return thresholdUpperLimit;
	}
	public void setThresholdUpperLimit(Double thresholdUpperLimit) {
		this.thresholdUpperLimit = thresholdUpperLimit;
	}
	@Override
	public String toString() {
		return "MonitorNumericValueInfo [monitorNumericType=" + monitorNumericType
				+ ", priority=" + priority + ", message=" + message + ", thresholdLowerLimit=" + thresholdLowerLimit
				+ ", thresholdUpperLimit=" + thresholdUpperLimit + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}