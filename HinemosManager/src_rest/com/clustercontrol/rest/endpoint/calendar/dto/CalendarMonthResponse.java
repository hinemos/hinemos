/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.OperationStatusEnum;

public class CalendarMonthResponse {

	private Integer day;
	@RestBeanConvertEnum
	private OperationStatusEnum operationStatus;

	public CalendarMonthResponse() {
	}
	
	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public OperationStatusEnum getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(OperationStatusEnum operationStatus) {
		this.operationStatus = operationStatus;
	}

	@Override
	public String toString() {
		return "CalendarMonthResponse [day=" + day + ", operationStatus=" + operationStatus + "]";
	}

}
