/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(from = NodeVariableInfo.class)
@RestBeanConvertIdClassSet(infoClass = NodeVariableInfo.class, idName = "id")
public class AgtNodeVariableInfoResponse {

	// ---- from NodeVariableInfoPK
	// private String facilityId;
	private String nodeVariableName;

	// ---- from NodeVariableInfo
	// private NodeVariableInfoPK id;
	private String nodeVariableValue;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Boolean searchTarget;

	public AgtNodeVariableInfoResponse() {
	}

	// ---- accessors

	public String getNodeVariableName() {
		return nodeVariableName;
	}

	public void setNodeVariableName(String nodeVariableName) {
		this.nodeVariableName = nodeVariableName;
	}

	public String getNodeVariableValue() {
		return nodeVariableValue;
	}

	public void setNodeVariableValue(String nodeVariableValue) {
		this.nodeVariableValue = nodeVariableValue;
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

}
