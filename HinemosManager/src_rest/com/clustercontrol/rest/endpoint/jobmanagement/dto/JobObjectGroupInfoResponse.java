/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ConditionTypeEnum;

public class JobObjectGroupInfoResponse {

	/** AND/OR */
	@RestBeanConvertEnum
	private ConditionTypeEnum conditionType = ConditionTypeEnum.AND;

	/** 待ち条件群の順番 */
	private Integer orderNo;

	/** 複数待ち条件を持つか */
	private Boolean isGroup = false;

	/** 待ち条件 */
	private ArrayList<JobObjectInfoResponse> jobObjectList;

	public JobObjectGroupInfoResponse() {
	}

	public ConditionTypeEnum getConditionType() {
		return conditionType;
	}

	public void setConditionType(ConditionTypeEnum conditionType) {
		this.conditionType = conditionType;
	}

	public ArrayList<JobObjectInfoResponse> getJobObjectList() {
		return jobObjectList;
	}

	public void setJobObjectList(ArrayList<JobObjectInfoResponse> jobObjectList) {
		this.jobObjectList = jobObjectList;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public Boolean getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}
}
