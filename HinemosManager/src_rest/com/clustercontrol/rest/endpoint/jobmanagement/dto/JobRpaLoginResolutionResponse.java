/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.jobmanagement.model.JobRpaLoginResolutionMstEntity;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = JobRpaLoginResolutionMstEntity.class)
public class JobRpaLoginResolutionResponse {
	/** 解像度 */
	private String resolution;

	public JobRpaLoginResolutionResponse() {
	}

	public JobRpaLoginResolutionResponse(String resolution) {
		this.resolution = resolution;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
}
