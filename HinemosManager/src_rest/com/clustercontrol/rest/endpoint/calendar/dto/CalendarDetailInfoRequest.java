/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto;

import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.DayTypeEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.WeekNoEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.WeekXthEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = CalendarDetailInfo.class, idName = "id")
public class CalendarDetailInfoRequest implements RequestDto {

	@RestItemName(value = MessageConstant.OREDER)
	@RestValidateInteger(notNull = true, minVal = 1, maxVal = 32768)
	private Integer orderNo;
	
	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;
	
	@RestItemName(value = MessageConstant.YEAR)
	@RestValidateInteger(minVal = 0, maxVal = 9999)
	private Integer yearNo;
	
	@RestItemName(value = MessageConstant.MONTH)
	@RestValidateInteger(minVal = 0, maxVal = 12)
	private Integer monthNo;
	
	@RestItemName(value = MessageConstant.CALENDAR_DETAIL_DATE_TYPE)
	@RestBeanConvertEnum
	private DayTypeEnum dayType;
	
	@RestItemName(value = MessageConstant.WEEKDAY)
	@RestBeanConvertEnum
	private WeekNoEnum weekNo;
	
	@RestItemName(value = MessageConstant.CALENDAR_DETAIL_XTH)
	@RestBeanConvertEnum
	private WeekXthEnum weekXth;
	
	@RestItemName(value = MessageConstant.DAY)
	@RestValidateInteger(minVal = 1, maxVal = 31)
	private Integer dayNo;
	
	@RestItemName(value = MessageConstant.CALENDAR_PATTERN_ID)
	@RestValidateString(minLen = 0, maxLen = 64)
	private String calPatternId;
	
	@RestItemName(value = MessageConstant.CALENDAR_DETAIL_BEFORE_AFTER)
	@RestValidateInteger(minVal = -32768, maxVal = 32768)
	private Integer afterDay;
	
	@RestItemName(value = MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_FLAG)
	private Boolean substituteFlg;
	
	@RestItemName(value = MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_TIME)
	@RestValidateInteger(minVal = -8784, maxVal = 8784)
	private Integer substituteTime;
	
	@RestItemName(value = MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_LIMIT)
	@RestValidateInteger(minVal = 1, maxVal = 99)
	private Integer substituteLimit;
	
	@RestItemName(value = MessageConstant.START)
	@RestValidateString(notNull = true)
	@RestBeanConvertIgnore
	private String startTime; // 個別変換(エポックミリ秒<->HH:mm:ss)
	
	@RestItemName(value = MessageConstant.END)
	@RestValidateString(notNull = true)
	@RestBeanConvertIgnore
	private String endTime; // 個別変換(エポックミリ秒<->HH:mm:ss)
	
	@RestItemName(value = MessageConstant.CALENDAR_EXECUTE_FLAG)
	private Boolean executeFlg;

	public CalendarDetailInfoRequest() {
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
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
