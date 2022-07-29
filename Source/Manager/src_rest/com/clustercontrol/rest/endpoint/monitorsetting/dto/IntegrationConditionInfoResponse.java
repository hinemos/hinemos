/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.analytics.model.IntegrationConditionInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.MonitorTypeEnum;

@RestBeanConvertIdClassSet(infoClass = IntegrationConditionInfo.class, idName = "id")
public class IntegrationConditionInfoResponse {
	private Integer orderNo;
	private String description;
	private Boolean monitorNode;
	private String targetFacilityId;
	private String targetMonitorId;
	@RestBeanConvertEnum
	private MonitorTypeEnum targetMonitorType;
	private String targetItemName;
	private String targetDisplayName;
	@RestPartiallyTransrateTarget
	private String targetItemDisplayName;
	private String comparisonMethod;
	private String comparisonValue;
	private Boolean isAnd;

	public IntegrationConditionInfoResponse() {
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
	public Boolean getMonitorNode() {
		return monitorNode;
	}
	public void setMonitorNode(Boolean monitorNode) {
		this.monitorNode = monitorNode;
	}
	public String getTargetFacilityId() {
		return targetFacilityId;
	}
	public void setTargetFacilityId(String targetFacilityId) {
		this.targetFacilityId = targetFacilityId;
	}
	public String getTargetMonitorId() {
		return targetMonitorId;
	}
	public void setTargetMonitorId(String targetMonitorId) {
		this.targetMonitorId = targetMonitorId;
	}
	public MonitorTypeEnum getTargetMonitorType() {
		return targetMonitorType;
	}
	public void setTargetMonitorType(MonitorTypeEnum targetMonitorType) {
		this.targetMonitorType = targetMonitorType;
	}
	public String getTargetItemName() {
		return targetItemName;
	}
	public void setTargetItemName(String targetItemName) {
		this.targetItemName = targetItemName;
	}
	public String getTargetDisplayName() {
		return targetDisplayName;
	}
	public void setTargetDisplayName(String targetDisplayName) {
		this.targetDisplayName = targetDisplayName;
	}
	public String getTargetItemDisplayName() {
		return targetItemDisplayName;
	}
	public void setTargetItemDisplayName(String targetItemDisplayName) {
		this.targetItemDisplayName = targetItemDisplayName;
	}
	public String getComparisonMethod() {
		return comparisonMethod;
	}
	public void setComparisonMethod(String comparisonMethod) {
		this.comparisonMethod = comparisonMethod;
	}
	public String getComparisonValue() {
		return comparisonValue;
	}
	public void setComparisonValue(String comparisonValue) {
		this.comparisonValue = comparisonValue;
	}
	public Boolean getIsAnd() {
		return isAnd;
	}
	public void setIsAnd(Boolean isAnd) {
		this.isAnd = isAnd;
	}
	@Override
	public String toString() {
		return "IntegrationConditionInfo [orderNo=" + orderNo + ", description="
				+ description + ", monitorNode=" + monitorNode + ", targetFacilityId=" + targetFacilityId
				+ ", targetMonitorId=" + targetMonitorId + ", targetMonitorType=" + targetMonitorType
				+ ", targetItemName=" + targetItemName + ", targetDisplayName=" + targetDisplayName
				+ ", targetItemDisplayName=" + targetItemDisplayName + ", comparisonMethod=" + comparisonMethod
				+ ", comparisonValue=" + comparisonValue + ", isAnd=" + isAnd + "]";
	}

}