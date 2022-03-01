/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.CommandExecTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.ConvertFlagEnum;

public class CustomCheckInfoRequest implements RequestDto {
	private String command;
	private Boolean specifyUser;
	private String effectiveUser;
	@RestBeanConvertEnum
	private CommandExecTypeEnum commandExecTypeCode;
	private String selectedFacilityId;
	private Integer timeout;
	@RestBeanConvertEnum
	private ConvertFlagEnum convertFlg;

	public CustomCheckInfoRequest() {
	}

	
	public String getCommand() {
		return command;
	}


	public void setCommand(String command) {
		this.command = command;
	}


	public Boolean getSpecifyUser() {
		return specifyUser;
	}


	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}


	public String getEffectiveUser() {
		return effectiveUser;
	}


	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}


	public CommandExecTypeEnum getCommandExecTypeCode() {
		return commandExecTypeCode;
	}


	public void setCommandExecType(CommandExecTypeEnum commandExecTypeCode) {
		this.commandExecTypeCode = commandExecTypeCode;
	}


	public String getSelectedFacilityId() {
		return selectedFacilityId;
	}


	public void setSelectedFacilityId(String selectedFacilityId) {
		this.selectedFacilityId = selectedFacilityId;
	}


	public Integer getTimeout() {
		return timeout;
	}


	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}


	public ConvertFlagEnum getConvertFlg() {
		return convertFlg;
	}


	public void setConvertFlg(ConvertFlagEnum convertFlg) {
		this.convertFlg = convertFlg;
	}


	@Override
	public String toString() {
		return "CustomCheckInfoRequest [command=" + command + ", specifyUser=" + specifyUser + ", effectiveUser="
				+ effectiveUser + ", commandExecTypeCode=" + commandExecTypeCode + ", selectedFacilityId=" + selectedFacilityId
				+ ", timeout=" + timeout + ", convertFlg=" + convertFlg + "]";
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}