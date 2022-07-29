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
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddCalendarRequest implements RequestDto {

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, maxLen=64)
	private String ownerRoleId;
	@RestItemName(value = MessageConstant.CALENDAR_NAME)
	@RestValidateString(notNull = true, maxLen = 256)
	private String calendarName;
	@RestItemName(value = MessageConstant.CALENDAR_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String calendarId;
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

	public AddCalendarRequest() {
	}
	
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
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
