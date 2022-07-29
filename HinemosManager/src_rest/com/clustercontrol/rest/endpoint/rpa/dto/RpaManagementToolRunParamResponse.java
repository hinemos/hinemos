/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaManagementToolRunParamTypeEnum;

public class RpaManagementToolRunParamResponse {
	/** パラメータID */
	private Integer paramId;
	/** パラメータ名 */
	private String paramName;
	/** パラメータ値 */
	private String paramValue;
	/** パラメータタイプ */
	@RestBeanConvertEnum
	private RpaManagementToolRunParamTypeEnum paramType;
	/** 必須/任意 */
	private Boolean required;
	/** 編集可/不可 */
	private Boolean editable;
	/** 複数指定する項目かどうかのフラグ */
	private Boolean arrayFlg;
	/** シナリオ入力パラメータかどうかのフラグ */
	private Boolean scenarioParamFlg;
	/** 説明 */
	@RestPartiallyTransrateTarget
	private String description;
	/** 表示順序 */
	private Integer orderNo;
	/** RPA管理ツールID */
	private String rpaManagementToolId;
	/** 実行種別 */
	private Integer runType;

	public RpaManagementToolRunParamResponse() {
	}

	public Integer getParamId() {
		return paramId;
	}

	public void setParamId(Integer paramId) {
		this.paramId = paramId;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public RpaManagementToolRunParamTypeEnum getParamType() {
		return paramType;
	}

	public void setParamType(RpaManagementToolRunParamTypeEnum paramType) {
		this.paramType = paramType;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public Boolean getArrayFlg() {
		return arrayFlg;
	}

	public void setArrayFlg(Boolean arrayFlg) {
		this.arrayFlg = arrayFlg;
	}

	public Boolean getScenarioParamFlg() {
		return scenarioParamFlg;
	}

	public void setScenarioParamFlg(Boolean scenarioParamFlg) {
		this.scenarioParamFlg = scenarioParamFlg;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
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

}
