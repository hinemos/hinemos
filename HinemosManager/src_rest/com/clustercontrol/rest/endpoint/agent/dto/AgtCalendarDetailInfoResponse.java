/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(from = CalendarDetailInfo.class, checksAccessors = false)
@RestBeanConvertIdClassSet(infoClass = CalendarDetailInfo.class, idName = "id")
public class AgtCalendarDetailInfoResponse {

	// ---- from CalendarDetailInfo
	private String calendarId;
	private Integer orderNo;
	private String description;
	private Integer yearNo;
	private Integer monthNo;
	private Integer dayType;
	private Integer weekNo;
	private Integer weekXth;
	private Integer dayNo;
	private String calPatternId;
	private Integer afterDay;
	private Boolean substituteFlg;
	private Integer substituteTime;
	private Integer substituteLimit;
	private Long startTime;
	private Long endTime;
	private Boolean executeFlg;
	// private CalendarInfo calInfoEntity;
	private AgtCalendarPatternInfoResponse calPatternInfo;

	public AgtCalendarDetailInfoResponse() {
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getYear() {
		return yearNo;
	}

	public void setYear(Integer yearNo) {
		this.yearNo = yearNo;
	}

	public Integer getMonth() {
		return monthNo;
	}

	public void setMonth(Integer monthNo) {
		this.monthNo = monthNo;
	}

	public Integer getDayType() {
		return dayType;
	}

	public void setDayType(Integer dayType) {
		this.dayType = dayType;
	}

	public Integer getDayOfWeek() {
		return weekNo;
	}

	public void setDayOfWeek(Integer weekNo) {
		this.weekNo = weekNo;
	}

	public Integer getDayOfWeekInMonth() {
		return weekXth;
	}

	public void setDayOfWeekInMonth(Integer weekXth) {
		this.weekXth = weekXth;
	}

	public Integer getDate() {
		return dayNo;
	}

	public void setDate(Integer dayNo) {
		this.dayNo = dayNo;
	}

	public String getCalPatternId() {
		return calPatternId;
	}

	public void setCalPatternId(String calPatternId) {
		this.calPatternId = calPatternId;
	}

	public Integer getAfterday() {
		return afterDay;
	}

	public void setAfterday(Integer afterDay) {
		this.afterDay = afterDay;
	}

	public Boolean getSubstituteFlg() {
		return substituteFlg;
	}

	public void setSubstituteFlg(Boolean substituteFlg) {
		this.substituteFlg = substituteFlg;
	}

	public Integer getSubstituteTime() {
		return substituteTime;
	}

	public void setSubstituteTime(Integer substituteTime) {
		this.substituteTime = substituteTime;
	}

	public Integer getSubstituteLimit() {
		return substituteLimit;
	}

	public void setSubstituteLimit(Integer substituteLimit) {
		this.substituteLimit = substituteLimit;
	}

	public Long getTimeFrom() {
		return startTime;
	}

	public void setTimeFrom(Long startTime) {
		this.startTime = startTime;
	}

	public Long getTimeTo() {
		return endTime;
	}

	public void setTimeTo(Long endTime) {
		this.endTime = endTime;
	}

	public Boolean getOperateFlg() {
		return executeFlg;
	}

	public void setOperateFlg(Boolean executeFlg) {
		this.executeFlg = executeFlg;
	}

	public AgtCalendarPatternInfoResponse getCalPatternInfo() {
		return calPatternInfo;
	}

	public void setCalPatternInfo(AgtCalendarPatternInfoResponse calPatternInfo) {
		this.calPatternInfo = calPatternInfo;
	}

}
