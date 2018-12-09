/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
	private Boolean setEnvironment;

	private String infoCommand;
	private String warnCommand;
	private String criticalCommand;
	private String unknownCommand;

	private String infoEffectiveUser;
	private String warnEffectiveUser;
	private String criticalEffectiveUser;
	private String unknownEffectiveUser;

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


	@Column(name="set_environment")
	public Boolean getSetEnvironment() {
		return this.setEnvironment;
	}

	public void setSetEnvironment(Boolean setEnvironment) {
		this.setEnvironment = setEnvironment;
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