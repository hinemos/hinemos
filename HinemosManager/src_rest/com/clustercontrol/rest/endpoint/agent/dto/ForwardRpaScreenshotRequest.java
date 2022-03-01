/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.jobmanagement.bean.RpaJobScreenshot;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaScreenshotTriggerTypeEnum;

@RestBeanConvertAssertion(to = RpaJobScreenshot.class)
public class ForwardRpaScreenshotRequest extends AgentRequestDto {
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String facilityId;
	private Long outputDate;
	@RestBeanConvertEnum
	private RpaScreenshotTriggerTypeEnum triggerType;

	public ForwardRpaScreenshotRequest() {
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Long getOutputDate() {
		return outputDate;
	}

	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}

	public RpaScreenshotTriggerTypeEnum getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(RpaScreenshotTriggerTypeEnum triggerType) {
		this.triggerType = triggerType;
	}
}
