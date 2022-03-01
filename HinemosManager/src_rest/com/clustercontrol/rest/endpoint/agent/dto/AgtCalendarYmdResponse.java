/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(from = YMD.class, checksAccessors = false)
@RestBeanConvertIdClassSet(infoClass = YMD.class, idName = "id")
public class AgtCalendarYmdResponse {

	// ---- from YMDPK
	private Integer yearNo;
	private Integer monthNo;
	private Integer dayNo;

	// ---- from YMD
	// private CalendarPatternInfo calPatternInfoEntity;

	public AgtCalendarYmdResponse() {
	}

	// ---- accessors

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

	public Integer getDay() {
		return dayNo;
	}

	public void setDay(Integer dayNo) {
		this.dayNo = dayNo;
	}

}
