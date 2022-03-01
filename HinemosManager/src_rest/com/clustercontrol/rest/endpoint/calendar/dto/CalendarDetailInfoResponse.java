/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto;

import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.DayTypeEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.WeekNoEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.WeekXthEnum;

@RestBeanConvertIdClassSet(infoClass=CalendarDetailInfo.class,idName="id")
public class CalendarDetailInfoResponse {
	private Integer orderNo;
	private String description;
	private Integer yearNo;
	private Integer monthNo;
	@RestBeanConvertEnum
	private DayTypeEnum dayType;
	@RestBeanConvertEnum
	private WeekNoEnum weekNo;
	@RestBeanConvertEnum
	private WeekXthEnum weekXth;
	private Integer dayNo;
	private String calPatternId;
	private Integer afterDay;
	private Boolean substituteFlg;
	private Integer substituteTime;
	private Integer substituteLimit;
	@RestBeanConvertIgnore
	private String startTime; // 個別変換(エポックミリ秒<->HH:mm:ss)
	@RestBeanConvertIgnore
	private String endTime; // 個別変換(エポックミリ秒<->HH:mm:ss)
	private Boolean executeFlg;
	private CalendarPatternInfoResponse calPatternInfo;

	public CalendarDetailInfoResponse() {
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

	public Integer getYearNo() {
		return yearNo;
	}

	public void setYearNo(Integer yearNo) {
		this.yearNo = yearNo;
	}

	public Integer getMonthNo() {
		return monthNo;
	}

	public void setMonthNo(Integer monthNo) {
		this.monthNo = monthNo;
	}

	public DayTypeEnum getDayType() {
		return dayType;
	}

	public void setDayType(DayTypeEnum dayType) {
		this.dayType = dayType;
	}

	public WeekNoEnum getWeekNo() {
		return weekNo;
	}

	public void setWeekNo(WeekNoEnum weekNo) {
		this.weekNo = weekNo;
	}

	public WeekXthEnum getWeekXth() {
		return weekXth;
	}

	public void setWeekXth(WeekXthEnum weekXth) {
		this.weekXth = weekXth;
	}

	public Integer getDayNo() {
		return dayNo;
	}

	public void setDayNo(Integer dayNo) {
		this.dayNo = dayNo;
	}

	public String getCalPatternId() {
		return calPatternId;
	}

	public void setCalPatternId(String calPatternId) {
		this.calPatternId = calPatternId;
	}

	public Integer getAfterDay() {
		return afterDay;
	}

	public void setAfterDay(Integer afterDay) {
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

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Boolean getExecuteFlg() {
		return executeFlg;
	}

	public void setExecuteFlg(Boolean executeFlg) {
		this.executeFlg = executeFlg;
	}
	public CalendarPatternInfoResponse getCalPatternInfo() {
		return calPatternInfo;
	}
	public void setCalPatternInfo(CalendarPatternInfoResponse calPatternInfo) {
		this.calPatternInfo = calPatternInfo;
	}
}
