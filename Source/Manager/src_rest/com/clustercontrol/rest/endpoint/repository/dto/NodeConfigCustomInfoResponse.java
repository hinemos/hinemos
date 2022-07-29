/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = com.clustercontrol.repository.model.NodeConfigCustomInfo.class, idName = "id")
public class NodeConfigCustomInfoResponse {

	private String settingCustomId;
	private String displayName;
	private String description;
	private String command;
	private Boolean specifyUser;
	private String effectiveUser;
	private Boolean validFlg;

	public NodeConfigCustomInfoResponse() {
	}

	public String getSettingCustomId() {
		return settingCustomId;
	}

	public void setSettingCustomId(String settingCustomId) {
		this.settingCustomId = settingCustomId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}
}
