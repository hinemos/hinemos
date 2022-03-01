/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.jobmanagement.bean.JobRuntimeParamDetail;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = JobRuntimeParamDetail.class)
public class AgtJobRuntimeParamDetailResponse {

	// ---- JobRuntimeParamDetail
	private String paramValue;
	private String description;

	public AgtJobRuntimeParamDetailResponse() {
	}

	// ---- accessorss

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
