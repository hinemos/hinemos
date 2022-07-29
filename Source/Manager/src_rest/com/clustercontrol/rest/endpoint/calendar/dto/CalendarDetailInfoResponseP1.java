/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;

public class CalendarDetailInfoResponseP1 {
	private Integer yearNo;
	private Integer monthNo;
	private Integer dayNo;
	@RestBeanConvertIgnore
	private String startTime; // 個別変換(エポックミリ秒<->HH:mm:ss)
	@RestBeanConvertIgnore
	private String endTime; // 個別変換(エポックミリ秒<->HH:mm:ss)
	private Boolean executeFlg;

	public CalendarDetailInfoResponseP1() {
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

	public Integer getDayNo() {
		return dayNo;
	}

	public void setDayNo(Integer dayNo) {
		this.dayNo = dayNo;
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
}
