/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class EventLogOperationHistoryEntityResponse {
	private Long logSeqNo;
	private String monitorId;
	private String monitorDetailId;
	private String pluginId;
	@RestBeanConvertDatetime
	private String outputDate;
	private String facilityId;
	@RestBeanConvertDatetime
	private String operationDate;
	private String operationUser;
	private Integer historyType;
	@RestPartiallyTransrateTarget
	private String detail;
	
	public EventLogOperationHistoryEntityResponse() {
	}

	public Long getLogSeqNo() {
		return logSeqNo;
	}

	public void setLogSeqNo(Long logSeqNo) {
		this.logSeqNo = logSeqNo;
	}

	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetailId() {
		return this.monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getPluginId() {
		return this.pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getOutputDate() {
		return this.outputDate;
	}
	public void setOutputDate(String outputDate) {
		this.outputDate = outputDate;
	}

	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	public String getOperationDate() {
		return this.operationDate;
	}
	public void setOperationDate(String operationDate) {
		this.operationDate = operationDate;
	}
	
	public String getOperationUser() {
		return this.operationUser;
	}
	public void setOperationUser(String operationUser) {
		this.operationUser = operationUser;
	}
	
	public Integer getHistoryType() {
		return this.historyType;
	}
	public void setHistoryType(Integer historyType) {
		this.historyType = historyType;
	}
	
	public String getDetail() {
		return this.detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}

}
