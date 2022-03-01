/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto;

import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass=YMD.class,idName="id")
public class YMDResponse {
	private Integer yearNo;
	private Integer monthNo;
	private Integer dayNo;

	public YMDResponse() {
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

}
