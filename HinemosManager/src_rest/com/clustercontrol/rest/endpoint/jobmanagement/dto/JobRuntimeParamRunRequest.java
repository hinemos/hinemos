/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class JobRuntimeParamRunRequest implements RequestDto {

	public JobRuntimeParamRunRequest() {
	}

	/** ジョブ変数名 */
	@RestItemName(value = MessageConstant.JOBKICK_PARAM_ID)
	@RestValidateString(maxLen = 64)
	private String paramId;

	/** ジョブ変数値 */
	private String value;

	public String getParamId() {
		return paramId;
	}

	public void setParamId(String paramId) {
		this.paramId = paramId;
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
