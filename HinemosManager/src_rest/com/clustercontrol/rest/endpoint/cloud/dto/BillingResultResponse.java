/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.xcloud.bean.BillingResult.TargetType;

public class BillingResultResponse {
	private TargetType type;
	private String targetId;
	private String targetName;
	private Integer targetYear;
	private Integer targetMonth;
	@RestBeanConvertDatetime
	private String beginTime;
	@RestBeanConvertDatetime
	private String endTime;
	private List<FacilityBillingResponse> facilities;
	private String unit;

	public BillingResultResponse() {
	}

	public TargetType getType() {
		return type;
	}

	public void setType(TargetType type) {
		this.type = type;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public Integer getTargetYear() {
		return targetYear;
	}

	public void setTargetYear(Integer targetYear) {
		this.targetYear = targetYear;
	}

	public Integer getTargetMonth() {
		return targetMonth;
	}

	public void setTargetMonth(Integer targetMonth) {
		this.targetMonth = targetMonth;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public List<FacilityBillingResponse> getFacilities() {
		return facilities;
	}

	public void setFacilities(List<FacilityBillingResponse> facilities) {
		this.facilities = facilities;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

}
