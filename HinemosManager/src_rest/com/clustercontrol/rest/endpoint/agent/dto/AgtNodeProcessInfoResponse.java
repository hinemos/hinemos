/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(from = NodeProcessInfo.class)
@RestBeanConvertIdClassSet(infoClass = NodeProcessInfo.class, idName = "id")
public class AgtNodeProcessInfoResponse {

	// ---- from NodeProcessInfoPK
	// private String facilityId;
	private String processName;
	private Integer pid;

	// ---- from NodeProcessInfo
	// private NodeProcessInfoPK id;
	private String path;
	private String execUser;
	private Long startupDateTime;
	private Long regDate;
	private String regUser;
	private Boolean searchTarget;

	public AgtNodeProcessInfoResponse() {
	}

	// ---- accessors

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getExecUser() {
		return execUser;
	}

	public void setExecUser(String execUser) {
		this.execUser = execUser;
	}

	public Long getStartupDateTime() {
		return startupDateTime;
	}

	public void setStartupDateTime(Long startupDateTime) {
		this.startupDateTime = startupDateTime;
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
