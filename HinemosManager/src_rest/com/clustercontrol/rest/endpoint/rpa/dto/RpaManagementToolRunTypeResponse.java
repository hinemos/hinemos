/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class RpaManagementToolRunTypeResponse {
	/** RPA管理ツールID */
	private String rpaManagementToolId;
	/** 実行種別 */
	private Integer runType;
	/** 実行種別名 */
	@RestPartiallyTransrateTarget
	private String runTypeName;

	public RpaManagementToolRunTypeResponse() {
	}

	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}

	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}

	public Integer getRunType() {
		return runType;
	}

	public void setRunType(Integer runType) {
		this.runType = runType;
	}

	public String getRunTypeName() {
		return runTypeName;
	}

	public void setRunTypeName(String runTypeName) {
		this.runTypeName = runTypeName;
	}

}
