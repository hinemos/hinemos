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

import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;

@RestBeanConvertIdClassSet(infoClass=YMD.class,idName="id")
public class CalendarPatternInfoResponse {
	private String ownerRoleId;
	private String calendarPatternId;
	private String calendarPatternName;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String updateUser;
	private List<YMDResponse> calPatternDetailInfoEntities = new ArrayList<>();

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

	public List<YMDResponse> getCalPatternDetailInfoEntities() {
		return calPatternDetailInfoEntities;
	}

	public void setCalPatternDetailInfoEntities(List<YMDResponse> calPatternDetailInfoEntities) {
		this.calPatternDetailInfoEntities = calPatternDetailInfoEntities;
	}
}
