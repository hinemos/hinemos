/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = CalendarPatternInfo.class, checksAccessors = false)
public class AgtCalendarPatternInfoResponse {

	// ---- from CalendarPatternInfo
	private String calendarPatternId;
	private String calendarPatternName;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private List<AgtCalendarYmdResponse> calPatternDetailInfoEntities;

	public AgtCalendarPatternInfoResponse() {
	}

	public String getCalendarPatternId() {
		return calendarPatternId;
	}

	public void setCalendarPatternId(String calendarPatternId) {
		this.calendarPatternId = calendarPatternId;
	}

	public String getCalendarPatternName() {
		return calendarPatternName;
	}

	public void setCalendarPatternName(String calendarPatternName) {
		this.calendarPatternName = calendarPatternName;
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

	public List<AgtCalendarYmdResponse> getYmd() {
		return calPatternDetailInfoEntities;
	}

	public void setYmd(List<AgtCalendarYmdResponse> ymdList) {
		this.calPatternDetailInfoEntities = ymdList;
	}

}
