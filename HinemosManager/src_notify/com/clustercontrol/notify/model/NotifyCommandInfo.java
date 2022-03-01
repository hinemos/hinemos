/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_notify_command_info database table.
 * 
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_command_info", schema="setting")
@Cacheable(true)
public class NotifyCommandInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer commandTimeout;

	private String infoCommand;
	private String warnCommand;
	private String criticalCommand;
	private String unknownCommand;

	private String infoEffectiveUser;
	private String warnEffectiveUser;
	private String criticalEffectiveUser;
	private String unknownEffectiveUser;

	private Integer commandSettingType;

	public NotifyCommandInfo() {
	}

	public NotifyCommandInfo(String notifyId) {
		super(notifyId);
	}

	@Column(name="info_command")
	public String getInfoCommand() {
		return infoCommand;
	}

	public void setInfoCommand(String infoCommand) {
		this.infoCommand = infoCommand;
	}

	@Column(name="warn_command")
	public String getWarnCommand() {
		return warnCommand;
	}

	public void setWarnCommand(String warnCommand) {
		this.warnCommand = warnCommand;
	}

	@Column(name="critical_command")
	public String getCriticalCommand() {
		return criticalCommand;
	}

	public void setCriticalCommand(String criticalCommand) {
		this.criticalCommand = criticalCommand;
	}

	@Column(name="unknown_command")
	public String getUnknownCommand() {
		return unknownCommand;
	}

	public void setUnknownCommand(String unknownCommand) {
		this.unknownCommand = unknownCommand;
	}


	@Column(name="info_effective_user")
	public String getInfoEffectiveUser() {
		return infoEffectiveUser;
	}

	public void setInfoEffectiveUser(String infoEffectiveUser) {
		this.infoEffectiveUser = infoEffectiveUser;
	}

	@Column(name="warn_effective_user")
	public String getWarnEffectiveUser() {
		return warnEffectiveUser;
	}

	public void setWarnEffectiveUser(String warnEffectiveUser) {
		this.warnEffectiveUser = warnEffectiveUser;
	}

	@Column(name="critical_effective_user")
	public String getCriticalEffectiveUser() {
		return criticalEffectiveUser;
	}

	public void setCriticalEffectiveUser(String criticalEffectiveUser) {
		this.criticalEffectiveUser = criticalEffectiveUser;
	}

	@Column(name="unknown_effective_user")
	public String getUnknownEffectiveUser() {
		return unknownEffectiveUser;
	}

	public void setUnknownEffectiveUser(String unknownEffectiveUser) {
		this.unknownEffectiveUser = unknownEffectiveUser;
	}

	@Column(name="command_timeout")
	public Integer getTimeout() {
		return this.commandTimeout;
	}

	public void setTimeout(Integer commandTimeout) {
		this.commandTimeout = commandTimeout;
	}

	@Column(name="command_setting_type")
	public Integer getCommandSettingType() {
		return commandSettingType;
	}

	public void setCommandSettingType(Integer commandSettingType) {
		this.commandSettingType = commandSettingType;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyCommandInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyCommandInfo(null);
	}
}