/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(to = NodeHostnameInfo.class)
@RestBeanConvertIdClassSet(infoClass = NodeHostnameInfo.class, idName = "id")
public class AgtNodeHostnameInfoRequest extends AgentRequestDto {

	// --- from NodeHostnameInfoPK
	// private String facilityId;
	private String hostname;

	// --- from NodeHostnameInfo
	// private NodeHostnameInfoPK id;
	private Long regDate;
	private String regUser;
	private Boolean searchTarget;

	public AgtNodeHostnameInfoRequest() {
	}

	// ---- accessors

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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

	public Boolean getSearchTarget() {
		return searchTarget;
	}

	public void setSearchTarget(Boolean searchTarget) {
		this.searchTarget = searchTarget;
	}

}
