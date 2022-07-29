/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.CommandSettingTypeEnum;

public class CommandNotifyDetailInfoResponse {
	private Boolean infoValidFlg;
	private Boolean warnValidFlg;
	private Boolean criticalValidFlg;
	private Boolean unknownValidFlg;

	private Integer commandTimeout;

	private String infoCommand;
	private String warnCommand;
	private String criticalCommand;
	private String unknownCommand;

	private String infoEffectiveUser;
	private String warnEffectiveUser;
	private String criticalEffectiveUser;
	private String unknownEffectiveUser;
	@RestBeanConvertEnum
	private CommandSettingTypeEnum commandSettingType;

	public CommandNotifyDetailInfoResponse() {
	}

	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	public Integer getCommandTimeout() {
		return commandTimeout;
	}

	public void setCommandTimeout(Integer commandTimeout) {
		this.commandTimeout = commandTimeout;
	}

	public String getInfoCommand() {
		return infoCommand;
	}

	public void setInfoCommand(String infoCommand) {
		this.infoCommand = infoCommand;
	}

	public String getWarnCommand() {
		return warnCommand;
	}

	public void setWarnCommand(String warnCommand) {
		this.warnCommand = warnCommand;
	}

	public String getCriticalCommand() {
		return criticalCommand;
	}

	public void setCriticalCommand(String criticalCommand) {
		this.criticalCommand = criticalCommand;
	}

	public String getUnknownCommand() {
		return unknownCommand;
	}

	public void setUnknownCommand(String unknownCommand) {
		this.unknownCommand = unknownCommand;
	}

	public String getInfoEffectiveUser() {
		return infoEffectiveUser;
	}

	public void setInfoEffectiveUser(String infoEffectiveUser) {
		this.infoEffectiveUser = infoEffectiveUser;
	}

	public String getWarnEffectiveUser() {
		return warnEffectiveUser;
	}

	public void setWarnEffectiveUser(String warnEffectiveUser) {
		this.warnEffectiveUser = warnEffectiveUser;
	}

	public String getCriticalEffectiveUser() {
		return criticalEffectiveUser;
	}

	public void setCriticalEffectiveUser(String criticalEffectiveUser) {
		this.criticalEffectiveUser = criticalEffectiveUser;
	}

	public String getUnknownEffectiveUser() {
		return unknownEffectiveUser;
	}

	public void setUnknownEffectiveUser(String unknownEffectiveUser) {
		this.unknownEffectiveUser = unknownEffectiveUser;
	}

	public CommandSettingTypeEnum getCommandSettingType() {
		return commandSettingType;
	}

	public void setCommandSettingType(CommandSettingTypeEnum commandSettingType) {
		this.commandSettingType = commandSettingType;
	}
}
