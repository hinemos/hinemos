/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class RpaManagementToolStopModeResponse {
	/** RPA管理ツールID */
	private String rpaManagementToolId;
	/** 停止方法 */
	private Integer stopMode;
	/** 停止方法名 */
	@RestPartiallyTransrateTarget
	private String stopModeName;

	public RpaManagementToolStopModeResponse() {
	}

	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}

	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}

	public Integer getStopMode() {
		return stopMode;
	}

	public void setStopMode(Integer stopMode) {
		this.stopMode = stopMode;
	}

	public String getStopModeName() {
		return stopModeName;
	}

	public void setStopModeName(String stopModeName) {
		this.stopModeName = stopModeName;
	}

}
