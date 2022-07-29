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
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



/**
 * RPA管理ツールマスタのEntity定義
 * 
 */
@Entity
@Table(name="cc_rpa_management_tool_mst", schema="setting")
public class RpaManagementToolMst implements Serializable {
	private static final long serialVersionUID = 1L;
	// RPA管理ツールID
	private String rpaManagementToolId;
	// APIバージョン(Hinemos独自の採番)
	private Integer apiVersion;
	// RPA管理ツール名
	private String rpaManagementToolName;
	// RPA管理ツールバージョン
	private String rpaManagementToolVersion;
	// RPA管理ツールタイプ
	private RpaManagementToolTypeMst rpaManagementToolType;

	public RpaManagementToolMst() {
	}


	// RPA管理ツールID
	@Id
	@Column(name="rpa_management_tool_id")
	public String getRpaManagementToolId() {
		return this.rpaManagementToolId;
	}

	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}


	// APIバージョン(Hinemos独自の採番)
	@Column(name="api_version")
	public Integer getApiVersion() {
		return this.apiVersion;
	}

	public void setApiVersion(Integer apiVersion) {
		this.apiVersion = apiVersion;
	}


	// RPA管理ツール名
	@Column(name="rpa_management_tool_name")
	public String getRpaManagementToolName() {
		return this.rpaManagementToolName;
	}

	public void setRpaManagementToolName(String rpaManagementToolName) {
		this.rpaManagementToolName = rpaManagementToolName;
	}


	// RPA管理ツールバージョン
	@Column(name="rpa_management_tool_version")
	public String getRpaManagementToolVersion() {
		return this.rpaManagementToolVersion;
	}

	public void setRpaManagementToolVersion(String rpaManagementToolVersion) {
		this.rpaManagementToolVersion = rpaManagementToolVersion;
	}


	// RPA管理ツールタイプ
	//uni-directional many-to-one association to RpaManagementToolTypeMst
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="rpa_management_tool_type")
	public RpaManagementToolTypeMst getRpaManagementToolType() {
		return this.rpaManagementToolType;
	}

	@Deprecated
	public void setRpaManagementToolType(RpaManagementToolTypeMst rpaManagementToolType) {
		this.rpaManagementToolType = rpaManagementToolType;
	}

}