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
 * RPA管理ツール実行種別マスタのPK定義
 * 
 */
@Embeddable
public class RpaManagementToolRunTypeMstPK implements Serializable {
	private static final long serialVersionUID = 1L;
	/** RPA管理ツールID */
	private String rpaManagementToolId;

	/** 実行種別 */
	private Integer runType;

	public RpaManagementToolRunTypeMstPK() {
	}

	public RpaManagementToolRunTypeMstPK(String rpaManagementToolId, Integer runType) {
		this.rpaManagementToolId = rpaManagementToolId;
		this.runType = runType;
	}

	@Column(name = "rpa_management_tool_id")
	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}

	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}

	@Column(name = "run_type")
	public Integer getRunType() {
		return runType;
	}

	public void setRunType(Integer runType) {
		this.runType = runType;
	}
}
