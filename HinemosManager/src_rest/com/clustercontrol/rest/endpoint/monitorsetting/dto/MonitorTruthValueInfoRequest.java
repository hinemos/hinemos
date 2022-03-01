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
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.TruthValueEnum;

@RestBeanConvertIdClassSet(infoClass = MonitorTruthValueInfo.class, idName = "id")
public class MonitorTruthValueInfoRequest implements RequestDto {
	@RestBeanConvertEnum
	private PriorityEnum priority;
	@RestBeanConvertEnum
	private TruthValueEnum truthValue;
	private String message;
	public MonitorTruthValueInfoRequest() {
	}
	public PriorityEnum getPriority() {
		return priority;
	}
	public void setPriority(PriorityEnum priority) {
		this.priority = priority;
	}
	public TruthValueEnum getTruthValue() {
		return truthValue;
	}
	public void setTruthValue(TruthValueEnum truthValue) {
		this.truthValue = truthValue;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "MonitorTruthValueInfo [priority=" + priority + ", truthValue=" + truthValue
				+ ", message=" + message + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}