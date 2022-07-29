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

import com.clustercontrol.rpa.scenario.model.RpaScenario.CulcType;

public class GetRpaScenarioResponse {
	
	public GetRpaScenarioResponse() {
	}

	/** シナリオID */
	private String scenarioId;
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** 説明 */
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
	private String scenarioIdentifyString;
	/** 複数ノード共通のシナリオ(チェックボックス) */
	private Boolean commonNodeScenario;
	/** シナリオ名 */
	private String scenarioName;
	/** オーナーロールID */
	private String ownerRoleId;
	
	/** 実行ノード*/
	private List<GetRpaScenarioExecNodeDataResponse> execNodeList = new ArrayList<>();
	
	/** シナリオタグ紐付け情報 */
	private List<RpaScenarioTagResponse> tagList = new ArrayList<>();


	/** シナリオID */
	public String getScenarioId() {
		return this.scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

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

	/** オーナーロールID */
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/** シナリオタグ紐付け情報 */
	public List<RpaScenarioTagResponse> getTagList() {
		return tagList;
	}
	public void setTagList(List<RpaScenarioTagResponse> tagList) {
		this.tagList = tagList;
	}
	
	/** シナリオタグ紐付け情報を追加*/
	public void addTagList(RpaScenarioTagResponse tag) {
		getTagList().add(tag);
	}

	/** 実行ノード情報*/
	public List<GetRpaScenarioExecNodeDataResponse> getExecNodeList() {
		return execNodeList;
	}
	public void setExecNodeList(List<GetRpaScenarioExecNodeDataResponse> execNodeList) {
		this.execNodeList = execNodeList;
	}
	
	/** 実行ノード情報を追加*/
	public void addExecNodeList(GetRpaScenarioExecNodeDataResponse execNode) {
		getExecNodeList().add(execNode);
	}
	
	@Override
	public String toString() {
		return "RpaScenarioResponse [scenarioId=" + scenarioId
				+ ", description=" + description + ", ownerRoleId=" + ownerRoleId + ", rpaToolId=" + rpaToolId
				+ ", manualTime=" + manualTime + ", manualTimeCulcType=" + manualTimeCulcType
				+ ", opeStartDate=" + opeStartDate + ", scenarioIdentifyString=" + scenarioIdentifyString 
				+ ", commonNodeScenario=" + commonNodeScenario + ", scenarioName=" + scenarioName + ", ownerRoleId=" + ownerRoleId
				+ ", execNodeList=" + execNodeList + ", tagRelationList=" + tagList + "]";
	}


}
