/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

 package com.clustercontrol.rpa.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


/**
 * RPA管理ツールタイプマスタのEntity定義
 * 
 */
@Entity
@Table(name="cc_rpa_management_tool_type_mst", schema="setting")
public class RpaManagementToolTypeMst implements Serializable {
	private static final long serialVersionUID = 1L;
	// RPA管理ツールタイプ
	private String rpaManagementToolType;
	// RPA管理ツールタイプ名
	private String rpaManagementToolTypeName;
	// API定義クラス名
	// @see RpaManagementRestDefine
	private String apiDefineClassName;
	
	// RPA管理ツール
	private List<RpaManagementToolMst> rpaManagementTool;

	public RpaManagementToolTypeMst() {
	}

	// RPA管理ツールタイプ
	@Id
	@Column(name="rpa_management_tool_type")
	public String getRpaManagementToolType() {
		return this.rpaManagementToolType;
	}


	public void setRpaManagementToolType(String rpaManagementToolType) {
		this.rpaManagementToolType = rpaManagementToolType;
	}

	// RPA管理ツールタイプ名
	@Column(name="rpa_management_tool_type_name")
	public String getRpaManagementToolTypeName() {
		return rpaManagementToolTypeName;
	}

	public void setRpaManagementToolTypeName(String rpaManagementToolTypeName) {
		this.rpaManagementToolTypeName = rpaManagementToolTypeName;
	}

	// API定義クラス名
	@Column(name="api_define_class_name")
	public String getApiDefineClassName() {
		return apiDefineClassName;
	}

	public void setApiDefineClassName(String apiDefineClassName) {
		this.apiDefineClassName = apiDefineClassName;
	}

	// RPA管理ツール
	@OneToMany(mappedBy="rpaManagementToolType", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<RpaManagementToolMst> getRpaManagementTool() {
		return rpaManagementTool;
	}

	public void setRpaManagementTool(List<RpaManagementToolMst> rpaManagementTool) {
		this.rpaManagementTool = rpaManagementTool;
	}
}