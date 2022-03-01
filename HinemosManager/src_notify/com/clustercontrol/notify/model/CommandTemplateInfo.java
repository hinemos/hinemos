/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.model;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * The persistent class for the cc_command_template_info database table.
 *
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_command_template", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_COMMAND_TEMPLATE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="command_template_id", insertable=false, updatable=false))
public class CommandTemplateInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String commandTemplateId;
	private String description;
	private String command;
	private Long createDate;
	private String createUser;
	private Long modifyDate;
	private String modifyUser;

	public CommandTemplateInfo() {
	}

	@Id
	@Column(name="command_template_id")
	public String getCommandTemplateId() {
		return this.commandTemplateId;
	}

	public void setCommandTemplateId(String commandTemplateId) {
		this.commandTemplateId = commandTemplateId;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="command")
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	@Column(name="create_datetime")
	public Long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	@Column(name="create_user_id")
	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	@Column(name="modify_datetime")
	public Long getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Long modifyDate) {
		this.modifyDate = modifyDate;
	}

	@Column(name="modify_user_id")
	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}
}