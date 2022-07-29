/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

public class RpaManagementToolResponse {
	// RPA管理ツールID
	private String rpaManagementToolId;
	// RPA管理ツール名
	private String rpaManagementToolName;
	// APIバージョン(Hinemos独自の採番)
	private Integer apiVersion;
	// RPA管理ツール種別
	private String rpaManagementToolType;
	public RpaManagementToolResponse() {
	}

	// RPA管理ツールID
	public String getRpaManagementToolId() {
		return this.rpaManagementToolId;
	}

	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}

	// RPA管理ツール名
	public String getRpaManagementToolName() {
		return this.rpaManagementToolName;
	}

	public void setRpaManagementToolName(String rpaManagementToolName) {
		this.rpaManagementToolName = rpaManagementToolName;
	}

	// APIバージョン(Hinemos独自の採番)
	public Integer getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(Integer apiVersion) {
		this.apiVersion = apiVersion;
	}

	// RPA管理ツール種別
	public String getRpaManagementToolType() {
		return rpaManagementToolType;
	}

	public void setRpaManagementToolType(String rpaManagementToolType) {
		this.rpaManagementToolType = rpaManagementToolType;
	}
	
	
}
