/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar.dto;

import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass=YMD.class,idName="id")
public class YMDRequest implements RequestDto {

	@RestItemName(value = MessageConstant.YEAR)
	@RestValidateInteger(notNull=true)
	private Integer yearNo;
	@RestItemName(value = MessageConstant.MONTH)
	@RestValidateInteger(notNull=true)
	private Integer monthNo;
	@RestItemName(value = MessageConstant.DAY)
	@RestValidateInteger(notNull=true)
	private Integer dayNo;
		
	public YMDRequest() {
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

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
