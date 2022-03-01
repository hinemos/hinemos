/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.infra.model.InfraCheckResult;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.InfraCheckResultEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = InfraCheckResult.class, idName = "id")
public class InfraCheckResultResponse {
	private String managementId;
	private String moduleId;
	private String nodeId;
	@RestBeanConvertEnum
	private InfraCheckResultEnum result;

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public InfraCheckResultEnum getResult() {
		return result;
	}

	public void setResult(InfraCheckResultEnum result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "InfraCheckResultResponse [managementId=" + managementId + ", moduleId=" + moduleId + ", nodeId="
				+ nodeId + ", result=" + result + "]";
	}
}
