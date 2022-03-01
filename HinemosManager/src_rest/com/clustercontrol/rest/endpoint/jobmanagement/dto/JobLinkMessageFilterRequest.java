/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

public class JobLinkMessageFilterRequest implements RequestDto {

	private String joblinkMessageId;
	private String srcFacilityId;
	private String srcFacilityName;
	private String monitorDetailId;
	private String application;
	@RestBeanConvertIgnore
	private List<PriorityEnum> priorityList;
	private String message = null;
	@RestBeanConvertDatetime
	private String sendFromDate = null;
	@RestBeanConvertDatetime
	private String sendToDate = null;
	@RestBeanConvertDatetime
	private String acceptFromDate = null;
	@RestBeanConvertDatetime
	private String acceptToDate = null;

	public JobLinkMessageFilterRequest() {
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public String getSrcFacilityId() {
		return srcFacilityId;
	}

	public void setSrcFacilityId(String srcFacilityId) {
		this.srcFacilityId = srcFacilityId;
	}

	public String getSrcFacilityName() {
		return srcFacilityName;
	}

	public void setSrcFacilityName(String srcFacilityName) {
		this.srcFacilityName = srcFacilityName;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public List<PriorityEnum> getPriorityList() {
		return priorityList;
	}

	public void setPriorityList(List<PriorityEnum> priorityList) {
		this.priorityList = priorityList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSendFromDate() {
		return sendFromDate;
	}

	public void setSendFromDate(String sendFromDate) {
		this.sendFromDate = sendFromDate;
	}

	public String getSendToDate() {
		return sendToDate;
	}

	public void setSendToDate(String sendToDate) {
		this.sendToDate = sendToDate;
	}

	public String getAcceptFromDate() {
		return acceptFromDate;
	}

	public void setAcceptFromDate(String acceptFromDate) {
		this.acceptFromDate = acceptFromDate;
	}

	public String getAcceptToDate() {
		return acceptToDate;
	}

	public void setAcceptToDate(String acceptToDate) {
		this.acceptToDate = acceptToDate;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
