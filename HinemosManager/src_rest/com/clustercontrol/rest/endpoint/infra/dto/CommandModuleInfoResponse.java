/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.AccessMethodTypeEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = InfraModuleInfo.class, idName = "id")
public class CommandModuleInfoResponse extends InfraModuleInfoResponse {
	@RestBeanConvertEnum
	private AccessMethodTypeEnum accessMethodType;
	private String execCommand;
	private String checkCommand;
	
	public AccessMethodTypeEnum getAccessMethodType() {
		return accessMethodType;
	}
	public void setAccessMethodType(AccessMethodTypeEnum accessMethodType) {
		this.accessMethodType = accessMethodType;
	}
	public String getExecCommand() {
		return execCommand;
	}
	public void setExecCommand(String execCommand) {
		this.execCommand = execCommand;
	}
	public String getCheckCommand() {
		return checkCommand;
	}
	public void setCheckCommand(String checkCommand) {
		this.checkCommand = checkCommand;
	}
	@Override
	public String toString() {
		return "CommandModuleInfoResponse [accessMethodType=" + accessMethodType + ", execCommand=" + execCommand
				+ ", checkCommand=" + checkCommand + "]";
	}
	
	
}
