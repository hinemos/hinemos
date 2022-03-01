/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.InfraNodeInputEnum;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class CreateSessionRequest implements RequestDto {

	@RestItemName(value = MessageConstant.INFRA_MANAGEMENT_ID)
	@RestValidateString(maxLen = 64, minLen = 1, notNull = true)
	private String managementId;
	
	private List<String> moduleIdList;
	
	@RestBeanConvertEnum
	private InfraNodeInputEnum nodeInputType;
	
	private List<AccessInfoRequest> accessList;

	public CreateSessionRequest() {
	}
	
	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}

	public List<String> getModuleIdList() {
		return moduleIdList;
	}

	public void setModuleIdList(List<String> moduleIdList) {
		this.moduleIdList = moduleIdList;
	}

	public InfraNodeInputEnum getNodeInputType() {
		return nodeInputType;
	}

	public void setNodeInputType(InfraNodeInputEnum nodeInputType) {
		this.nodeInputType = nodeInputType;
	}

	public List<AccessInfoRequest> getAccessList() {
		return accessList;
	}

	public void setAccessList(List<AccessInfoRequest> accessList) {
		this.accessList = accessList;
	}

	@Override
	public String toString() {
		return "CreateSessionRequest [managementId=" + managementId + ", moduleIdList=" + moduleIdList
				+ ", nodeInputType=" + nodeInputType + ", accessList=" + accessList + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
