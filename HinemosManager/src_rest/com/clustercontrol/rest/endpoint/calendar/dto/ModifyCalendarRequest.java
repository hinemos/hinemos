/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyCalendarRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.CALENDAR_NAME)
	@RestValidateString(notNull = true, maxLen = 256)
	private String calendarName;
	
	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;
	
	@RestItemName(value = MessageConstant.CALENDER_VALID_TIME_TO)
	@RestValidateString(notNull = true)
	@RestBeanConvertDatetime
	private String validTimeTo;
	
	@RestItemName(value = MessageConstant.CALENDER_VALID_TIME_FROM)
	@RestValidateString(notNull = true)
	@RestBeanConvertDatetime
	private String validTimeFrom;

	private List<CalendarDetailInfoRequest> calendarDetailList = new ArrayList<>();

	public ModifyCalendarRequest() {
	}

	public String getCalendarName() {
		return calendarName;
	}

	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getValidTimeFrom() {
		return validTimeFrom;
	}

	public void setValidTimeFrom(String validTimeFrom) {
		this.validTimeFrom = validTimeFrom;
	}

	public String getValidTimeTo() {
		return validTimeTo;
	}

	public void setValidTimeTo(String validTimeTo) {
		this.validTimeTo = validTimeTo;
	}

	public List<CalendarDetailInfoRequest> getCalendarDetailList() {
		return calendarDetailList;
	}

	public void setCalendarDetailList(List<CalendarDetailInfoRequest> calendarDetailList) {
		this.calendarDetailList = calendarDetailList;
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
