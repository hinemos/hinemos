/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.infra.model.InfraManagementParamInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = InfraManagementParamInfo.class, idName = "id")
public class InfraManagementParamInfoRequest implements RequestDto {

	@RestItemName(MessageConstant.INFRA_PARAM_ID)
	@RestValidateString(maxLen = 64, minLen = 1, notNull = true)
	private String paramId;

	@RestValidateObject(notNull = true)
	private Boolean passwordFlg;

	@RestItemName(MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256, minLen = 0)
	private String description;

	@RestItemName(MessageConstant.INFRA_PARAM_VALUE)
	@RestValidateString(maxLen = 1024, minLen = 0)
	private String value;

	public String getParamId() {
		return paramId;
	}

	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public Boolean getPasswordFlg() {
		return passwordFlg;
	}

	public void setPasswordFlg(Boolean passwordFlg) {
		this.passwordFlg = passwordFlg;
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

	@Override
	public String toString() {
		return "InfraManagementParamInfoRequest [paramId=" + paramId + ", passwordFlg=" + passwordFlg + ", description="
				+ description + ", value=" + value + "]";
	}

}
