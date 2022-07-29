/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(to = NodeCustomInfo.class)
@RestBeanConvertIdClassSet(infoClass = NodeCustomInfo.class, idName = "id")
public class AgtNodeCustomInfoRequest extends AgentRequestDto {

	// ---- from NodeCustomInfoPK
	private String facilityId;
	private String settingId;
	private String settingCustomId;

	// ---- from NodeCustomInfo
	// private NodeCustomInfoPK id;
	private String displayName;
	private String command;
	private String value;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Boolean searchTarget;
	private Integer registerFlag;

	public AgtNodeCustomInfoRequest() {
	}

	// ---- accessors

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getSettingId() {
		return settingId;
	}

	public void setSettingId(String settingId) {
		this.settingId = settingId;
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

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Boolean getSearchTarget() {
		return searchTarget;
	}

	public void setSearchTarget(Boolean searchTarget) {
		this.searchTarget = searchTarget;
	}

	public Integer getRegisterFlag() {
		return registerFlag;
	}

	public void setRegisterFlag(Integer registerFlag) {
		this.registerFlag = registerFlag;
	}

}
