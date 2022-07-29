/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.AccessMethodTypeEnum;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass=InfraModuleInfo.class,idName="id")
public class CommandModuleInfoRequest extends InfraModuleInfoRequest {
	
	@RestBeanConvertEnum
	private AccessMethodTypeEnum accessMethodType;
	
	@RestItemName(value = MessageConstant.INFRA_MODULE_EXEC_COMMAND)
	@RestValidateString(maxLen = 1024, minLen = 1)
	private String execCommand;
	
	@RestItemName(value = MessageConstant.INFRA_MODULE_CHECK_COMMAND)
	@RestValidateString(maxLen = 1024, minLen = 0)
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
		return "CommandModuleInfoRequest [accessMethodType=" + accessMethodType + ", execCommand=" + execCommand
				+ ", checkCommand=" + checkCommand + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
