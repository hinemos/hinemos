/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.jobmanagement.bean.RpaJobScreenshot;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

@RestBeanConvertAssertion(to = RpaJobScreenshot.class)
public class JobRpaScreenshotResponse {
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String facilityId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String outputDate;
	@RestPartiallyTransrateTarget
	private String description;

	public JobRpaScreenshotResponse() {
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
	public String getOutputDate() {
		return outputDate;
	}
	public void setOutputDate(String outputDate) {
		this.outputDate = outputDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
