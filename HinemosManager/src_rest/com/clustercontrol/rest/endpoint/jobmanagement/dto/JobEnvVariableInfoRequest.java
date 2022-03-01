/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class JobEnvVariableInfoRequest implements RequestDto {
	/** 環境変数ID */
	private String envVariableId;

	/** 説明 */
	private String description;

	/** 値 */
	private String value;

	public JobEnvVariableInfoRequest() {
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

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
