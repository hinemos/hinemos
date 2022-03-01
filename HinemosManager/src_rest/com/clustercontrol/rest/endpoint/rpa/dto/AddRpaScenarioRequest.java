/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rpa.scenario.model.RpaScenario.CulcType;
import com.clustercontrol.util.MessageConstant;
public class AddRpaScenarioRequest implements RequestDto {
	
	public AddRpaScenarioRequest(){
	}

	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** 説明 */
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	/** RPAツールID */
	private String rpaToolId;
	/** 手動操作時間 */
	private Long manualTime;
	/** 手動操作時間算出方式 */
	private CulcType manualTimeCulcType;
	/** 運用開始日時 */
	private Long opeStartDate;
	/** シナリオ識別文字列 */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_SCENARIO_IDENTIFY_STRING)
	@RestValidateString(notNull = true, maxLen = 512)
	private String scenarioIdentifyString;
	/** 複数ノード共通のシナリオ(チェックボックス) */
	private Boolean commonNodeScenario;
	/** シナリオ名 */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_NAME)
	@RestValidateString(notNull = true, maxLen = 512)
	private String scenarioName;
	
	/** 実行ノード */
	private List<String> execNodes = new ArrayList<>();
	
	/** シナリオタグ紐付け情報 */
	private List<String> tagRelationList;

	/** 作成日時 */
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	/** 新規作成ユーザ */
	private String regUser;
	/** 最終変更日時 */
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	/** 最終変更ユーザ */
	private String updateUser;


	/** シナリオ実績作成設定ID */
	public String getScenarioOperationResultCreateSettingId() {
		return scenarioOperationResultCreateSettingId;
	}
	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}

	/** 説明 */
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/** RPAツールID */
	public String getRpaToolId() {
		return rpaToolId;
	}
	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** 手動操作時間 */
	public Long getManualTime() {
		return this.manualTime;
	}
	public void setManualTime(Long manualTime) {
		this.manualTime = manualTime;
	}

	/** 手動操作時間算出方式 */
	public CulcType getManualTimeCulcType() {
		return this.manualTimeCulcType;
	}
	public void setManualTimeCulcType(CulcType manualTimeCulcType) {
		this.manualTimeCulcType = manualTimeCulcType;
	}

	/** 運用開始日時 */
	public Long getOpeStartDate() {
		return this.opeStartDate;
	}
	public void setOpeStartDate(Long opeStartDate) {
		this.opeStartDate = opeStartDate;
	}

	/** シナリオ識別文字列 */
	public String getScenarioIdentifyString() {
		return this.scenarioIdentifyString;
	}
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}

	/** シナリオ判定方式 */
	public Boolean getCommonNodeScenario() {
		return this.commonNodeScenario;
	}
	public void setCommonNodeScenario(Boolean commonNodeScenario) {
		this.commonNodeScenario = commonNodeScenario;
	}

	/** シナリオ名 */
	public String getScenarioName() {
		return this.scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	/** 実行ノード */
	public List<String> getExecNodes() {
		return execNodes;
	}
	public void setExecNodes(List<String> execNodes) {
		this.execNodes = execNodes;
	}

	/** シナリオタグ紐付け情報 */
	public List<String> getTagRelationList() {
		return tagRelationList;
	}
	public void setTagRelationList(List<String> tagRelationList) {
		this.tagRelationList = tagRelationList;
	}

	/** 作成日時 */
	public String getRegDate() {
		return this.regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	/** 新規作成ユーザ */
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	/** 最終変更日時 */
	public String getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	/** 最終変更ユーザ */
	public String getUpdateUser() {
		return this.updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Override
	public String toString() {
		return "AddRpaScenarioRequest [description=" + description + ", rpaToolId=" + rpaToolId + ", manualTime=" + manualTime + ", manualTimeCulcType="
				+ manualTimeCulcType + ", opeStartDate=" + opeStartDate + ", scenarioIdentifyString=" + scenarioIdentifyString + ", commonNodeScenario="
				+ commonNodeScenario + ", scenarioName=" + scenarioName + ", execNodes=" + execNodes + ", tagRelationList=" + tagRelationList + ", regDate=" + regDate
				+ ", regUser=" + regUser + ", updateDate=" + updateDate + ", updateUser=" + updateUser + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
