/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * RPA管理ツール停止方法マスタのPK定義
 * 
 */
@Embeddable
public class RpaManagementToolStopModeMstPK implements Serializable {
	private static final long serialVersionUID = 1L;
	/** RPA管理ツールID */
	private String rpaManagementToolId;

	/** 実行種別 */
	private Integer stopMode;

	public RpaManagementToolStopModeMstPK() {
	}

	public RpaManagementToolStopModeMstPK(String rpaManagementToolId, Integer stopMode) {
		this.rpaManagementToolId = rpaManagementToolId;
		this.stopMode = stopMode;
	}

	@Column(name = "rpa_management_tool_id")
	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}

	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}

	@Column(name = "stop_mode")
	public Integer getStopMode() {
		return stopMode;
	}

	public void setStopMode(Integer stopMode) {
		this.stopMode = stopMode;
	}
}
