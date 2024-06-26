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
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddCalendarPatternRequest implements RequestDto{

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, maxLen = 64)
	private String ownerRoleId;
	
	@RestItemName(value = MessageConstant.CALENDAR_PATTERN_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String calendarPatternId;
	
	@RestItemName(value = MessageConstant.CALENDAR_PATTERN_NAME)
	@RestValidateString(notNull = true, maxLen = 128)
	private String calendarPatternName;
	
	private List<YMDRequest> calPatternDetailInfoEntities = new ArrayList<>();

	public AddCalendarPatternRequest() {
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

	public List<YMDRequest> getCalPatternDetailInfoEntities() {
		return calPatternDetailInfoEntities;
	}

	public void setCalPatternDetailInfoEntities(List<YMDRequest> calPatternDetailInfoEntities) {
		this.calPatternDetailInfoEntities = calPatternDetailInfoEntities;
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
