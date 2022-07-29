/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = CalendarInfo.class)
public class AgtCalendarInfoResponse {

	// ---- CalendarInfo
	private String calendarId;
	private String calendarName;
	private String description;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Long validTimeFrom;
	private Long validTimeTo;
	private List<AgtCalendarDetailInfoResponse> calendarDetailList;

	public AgtCalendarInfoResponse() {
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

	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getValidTimeFrom() {
		return validTimeFrom;
	}

	public void setValidTimeFrom(Long validTimeFrom) {
		this.validTimeFrom = validTimeFrom;
	}

	public Long getValidTimeTo() {
		return validTimeTo;
	}

	public void setValidTimeTo(Long validTimeTo) {
		this.validTimeTo = validTimeTo;
	}

	public List<AgtCalendarDetailInfoResponse> getCalendarDetailList() {
		return calendarDetailList;
	}

	public void setCalendarDetailList(List<AgtCalendarDetailInfoResponse> calDetailInfoEntities) {
		this.calendarDetailList = calDetailInfoEntities;
	}

}
