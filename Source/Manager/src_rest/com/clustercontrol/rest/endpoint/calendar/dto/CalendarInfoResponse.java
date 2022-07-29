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

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;

public class CalendarInfoResponse {
	private String calendarId;
	private String calendarName;
	private String description;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String validTimeTo;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String validTimeFrom;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String updateUser;
	private String ownerRoleId;
	private List<CalendarDetailInfoResponse> calendarDetailList = new ArrayList<>();
	
	public CalendarInfoResponse() {
	}
	
	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
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

	public List<CalendarDetailInfoResponse> getCalendarDetailList() {
		return calendarDetailList;
	}

	public void setCalendarDetailList(List<CalendarDetailInfoResponse> calendarDetailList) {
		this.calendarDetailList = calendarDetailList;
	}

}
