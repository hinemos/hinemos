/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.infra.model.InfraManagementParamInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = InfraManagementParamInfo.class, idName = "id")
public class InfraManagementParamInfoResponse {

	private String paramId;
	private Boolean passwordFlg;
	private String description;
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
	public String toString() {
		return "InfraManagementParamInfoResponse [paramId=" + paramId + ", passwordFlg=" + passwordFlg
				+ ", description=" + description + ", value=" + value + "]";
	}
}
