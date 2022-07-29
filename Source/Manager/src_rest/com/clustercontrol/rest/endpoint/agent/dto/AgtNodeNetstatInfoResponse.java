/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(from = NodeNetstatInfo.class)
@RestBeanConvertIdClassSet(infoClass = NodeNetstatInfo.class, idName = "id")
public class AgtNodeNetstatInfoResponse {

	// ---- from NodeNetstatInfoPK
	// private String facilityId;
	private String protocol;
	private String localIpAddress;
	private String localPort;
	private String foreignIpAddress;
	private String foreignPort;
	private String processName;
	private Integer pid;

	// ---- from NodeNetstatInfo
	// private NodeNetstatInfoPK id;
	private String status;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Boolean searchTarget;

	public AgtNodeNetstatInfoResponse() {
	}

	// ---- accessors

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getLocalIpAddress() {
		return localIpAddress;
	}

	public void setLocalIpAddress(String localIpAddress) {
		this.localIpAddress = localIpAddress;
	}

	public String getLocalPort() {
		return localPort;
	}

	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	public String getForeignIpAddress() {
		return foreignIpAddress;
	}

	public void setForeignIpAddress(String foreignIpAddress) {
		this.foreignIpAddress = foreignIpAddress;
	}

	public String getForeignPort() {
		return foreignPort;
	}

	public void setForeignPort(String foreignPort) {
		this.foreignPort = foreignPort;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
