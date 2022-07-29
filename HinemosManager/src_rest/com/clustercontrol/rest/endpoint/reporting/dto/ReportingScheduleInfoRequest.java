/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportScheduleTypeEnum;

public class ReportingScheduleInfoRequest implements RequestDto {
	
	public ReportingScheduleInfoRequest(){
	}
	
	/** スケジュール種別 */
	@RestBeanConvertEnum
	private ReportScheduleTypeEnum type;

	private Integer month = null;

	private Integer day = null;

	private Integer week = null;

	private Integer hour = null;

	private Integer minute = null;

	public ReportScheduleTypeEnum getScheduleType() {
		return type;
	}

	public void setScheduleType(ReportScheduleTypeEnum type) {
		this.type = type;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
