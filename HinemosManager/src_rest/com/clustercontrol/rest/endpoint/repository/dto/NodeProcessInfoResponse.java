/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;

@RestBeanConvertIdClassSet(infoClass = NodeProcessInfo.class, idName = "id")
public class NodeProcessInfoResponse {

	private String processName;
	private Integer pid;

	private String path;
	private String execUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String startupDateTime;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	private Boolean searchTarget;

	public NodeProcessInfoResponse() {
	}

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

	public String getStartupDateTime() {
		return startupDateTime;
	}

	public void setStartupDateTime(String startupDateTime) {
		this.startupDateTime = startupDateTime;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
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
