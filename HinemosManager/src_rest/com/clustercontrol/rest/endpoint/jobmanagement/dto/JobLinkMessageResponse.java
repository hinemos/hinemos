/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

/**
 * ジョブ連携メッセージResponse
 * 
 * ジョブ連携メッセージのメッセージ、オリジナルメッセージはジョブ連携メッセージ送信時に
 * 送信元ノードでメッセージ変換をおこなってから送信されるため、
 * RestPartiallyTransrateTargetアノテーションは外してある。
 *
 */
public class JobLinkMessageResponse {

	private String joblinkMessageId;
	private String facilityId;
	@RestBeanConvertDatetime
	private String sendDate;
	@RestBeanConvertDatetime
	private String acceptDate;
	private String facilityName;
	private String ipAddress;
	private String monitorDetailId;
	private String application;
	@RestBeanConvertEnum
	private PriorityEnum priority;
	private String message;
	private String messageOrg;
	private List<JobLinkExpInfoResponse> jobLinkExpInfo = new ArrayList<>();

	public JobLinkMessageResponse() {
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

	public String getAcceptDate() {
		return acceptDate;
	}

	public void setAcceptDate(String acceptDate) {
		this.acceptDate = acceptDate;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
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

	public PriorityEnum getPriority() {
		return priority;
	}

	public void setPriority(PriorityEnum priority) {
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageOrg() {
		return messageOrg;
	}

	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}

	public List<JobLinkExpInfoResponse> getJobLinkExpInfo() {
		return jobLinkExpInfo;
	}

	public void setJobLinkExpInfo(List<JobLinkExpInfoResponse> jobLinkExpInfo) {
		this.jobLinkExpInfo = jobLinkExpInfo;
	}
}
