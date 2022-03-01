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

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rpa.scenario.model.RpaScenario.CulcType;

public class RpaScenarioResponse {
	
	public RpaScenarioResponse() {
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

	/** 実行ノード */
	public List<String> getExecNodes() {
		return execNodes;
	}
	public void setExecNodes(List<String> execNodes) {
		this.execNodes = execNodes;
	}
	
	/** 実行ノードを追加 */
	public void addExecNode(String execNode) {
		getExecNodes().add(execNode);
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
		return "RpaScenarioResponse [scenarioId=" + scenarioId
				+ ", description=" + description + ", ownerRoleId=" + ownerRoleId + ", rpaToolId=" + rpaToolId
				+ ", manualTime=" + manualTime + ", manualTimeCulcType=" + manualTimeCulcType
				+ ", opeStartDate=" + opeStartDate + ", scenarioIdentifyString=" + scenarioIdentifyString 
				+ ", commonNodeScenario=" + commonNodeScenario + ", scenarioName=" + scenarioName + ", ownerRoleId=" + ownerRoleId
				+ ", regDate = " + regDate + ", updateDate=" + updateDate + ", regUser=" + regUser + ", updateUser=" + updateUser
				+ ", execNodes=" + execNodes + ", tagRelationList=" + tagRelationList + "]";
	}

}
