/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = JobEnvVariableInfo.class)
public class AgtJobEnvVariableInfoResponse {

	// ---- from JobEnvVariableInfo
	private String envVariableId;
	private String description;
	private String value;

	public AgtJobEnvVariableInfoResponse() {
	}

	public String getEnvVariableId() {
		return envVariableId;
	}

	public void setEnvVariableId(String envVariableId) {
		this.envVariableId = envVariableId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
