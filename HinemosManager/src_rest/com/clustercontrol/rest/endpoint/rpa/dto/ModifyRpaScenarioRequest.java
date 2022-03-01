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
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rpa.scenario.model.RpaScenario.CulcType;
import com.clustercontrol.util.MessageConstant;

public class ModifyRpaScenarioRequest implements RequestDto {
	
	public ModifyRpaScenarioRequest (){}

	/** 説明 */
	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;
	/** 手動操作時間 */
	private Long manualTime;
	/** 手動操作時間算出方式 */
	private CulcType manualTimeCulcType;
	/** 運用開始日時 */
	private Long opeStartDate;
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


	/** 説明 */
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public List<String>  getExecNodes() {
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

	@Override
	public String toString() {
		return "ModifyRpaScenarioRequest [" 
				+ ", description=" + description + ", opeStartDate=" + opeStartDate 
				+ ", manualTime=" + manualTime + ", manualTimeCulcType=" + manualTimeCulcType
				+ ", commonNodeScenario=" + commonNodeScenario + ", scenarioName=" + scenarioName 
				+ ", execNodes=" + execNodes + ", tagRelationList=" + tagRelationList + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
