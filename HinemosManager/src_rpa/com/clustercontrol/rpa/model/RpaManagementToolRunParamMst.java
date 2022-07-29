/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * RPA管理ツールのシナリオ実行リクエストパラメータマスタのEntity定義<br>
 * 
 */
@Entity
@Table(name = "cc_rpa_management_tool_run_param_mst", schema = "setting")
@Cacheable(true)
public class RpaManagementToolRunParamMst implements Serializable {
	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	/** パラメータID */
	private Integer paramId;
	/** パラメータ名 */
	private String paramName;
	/** パラメータ値 */
	private String paramValue;
	/** パラメータタイプ */
	private Integer paramType;
	/** 必須/任意 */
	private Boolean required;
	/** 編集可/不可 */
	private Boolean editable;
	/** 複数指定する項目かどうかのフラグ */
	private Boolean arrayFlg;
	/** シナリオ入力パラメータかどうかのフラグ */
	private Boolean scenarioParamFlg;
	/** 説明 */
	private String description;
	/** 表示順序 */
	private Integer orderNo;
	/** RPA管理ツールID */
	private String rpaManagementToolId;
	/** 実行種別 */
	private Integer runType;

	public RpaManagementToolRunParamMst() {
	}

	@Id
	@Column(name = "param_id")
	public Integer getParamId() {
		return paramId;
	}

	public void setParamId(Integer paramId) {
		this.paramId = paramId;
	}

	@Column(name = "param_name")
	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	@Column(name = "param_value")
	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Column(name = "param_type")
	public Integer getParamType() {
		return paramType;
	}

	public void setParamType(Integer paramType) {
		this.paramType = paramType;
	}

	@Column(name = "required")
	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	@Column(name = "editable")
	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	@Column(name = "array_flg")
	public Boolean getArrayFlg() {
		return arrayFlg;
	}

	public void setArrayFlg(Boolean arrayFlg) {
		this.arrayFlg = arrayFlg;
	}

	@Column(name = "scenario_param_flg")
	public Boolean getScenarioParamFlg() {
		return scenarioParamFlg;
	}

	public void setScenarioParamFlg(Boolean scenarioParamFlg) {
		this.scenarioParamFlg = scenarioParamFlg;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "order_no")
	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
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
