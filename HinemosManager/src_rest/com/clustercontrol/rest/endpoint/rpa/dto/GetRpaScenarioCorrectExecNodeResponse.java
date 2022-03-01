/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

public class GetRpaScenarioCorrectExecNodeResponse {
	
	public GetRpaScenarioCorrectExecNodeResponse() {
	}

	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** シナリオ識別文字列 */
	private String scenarioIdentifyString;
	/** RPAツールID */
	private String rpaToolId;
	
	/** 同一作成設定ID、同一識別子のシナリオ一覧 */
	private List<RpaScenarioResponseP1> scenarioList;
	
	/** 実行ノード */
	private List<RpaScenarioExecNodeResponse> execNodes;
	
	/** シナリオ実績作成設定ID */
	public String getScenarioOperationResultCreateSettingId() {
		return scenarioOperationResultCreateSettingId;
	}
	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}

	/** シナリオ識別文字列 */
	public String getScenarioIdentifyString() {
		return scenarioIdentifyString;
	}
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}

	/** 実行ノード */
	public List<RpaScenarioExecNodeResponse> getExecNodeList() {
		return execNodes;
	}
	public void setExecNodeList(List<RpaScenarioExecNodeResponse> execNodeList) {
		this.execNodes = execNodeList;
	}

	/** RPAツールID */
	public String getRpaToolId() {
		return rpaToolId;
	}
	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	public List<RpaScenarioResponseP1> getScenarioList() {
		return scenarioList;
	}
	public void setScenarioList(List<RpaScenarioResponseP1> scenarioList) {
		this.scenarioList = scenarioList;
	}
	@Override
	public String toString() {
		return "RpaScenarioResponse [scenarioOperationResultCreateSettingId=" + scenarioOperationResultCreateSettingId
				+ ", scenarioIdentifyString=" + scenarioIdentifyString + ", execNodes=" + execNodes
				+ "]";
	}

}
