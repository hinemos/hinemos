/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * RPAシナリオ実績ビューのフィルタ設定を格納するクラス
 */
@XmlType(namespace = "http://rpa.ws.clustercontrol.com")
public class RpaScenarioFilterInfo implements Serializable {

	private static final long serialVersionUID = -70692603990721960L;
	
	/** RPAツールID */
	private String rpaToolId = null;
	/** RPAシナリオID */
	private String scenarioId = null;
	/** RPAシナリオ名 */
	private String scenarioName = null;
	/** RPAシナリオ識別文字列 */
	private String scenarioIdentifyString = null;
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** オーナーロールID */
	private String ownerRoleId = null;

	/** RPAツールID */
	public String getRpaToolId() {
		return rpaToolId;
	}
	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** RPAシナリオID */
	public String getScenarioId() {
		return scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

	/** RPAシナリオ名 */
	public String getScenarioName() {
		return scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	/** RPAシナリオ識別文字列 */
	public String getScenarioIdentifyString() {
		return scenarioIdentifyString;
	}
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}

	/** シナリオ実績作成設定ID */
	public String getScenarioOperationResultCreateSettingId() {
		return scenarioOperationResultCreateSettingId;
	}
	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}
	
	/** オーナーロールID */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	@Override
	public String toString() {
		return "RpaScenarioFilterInfo ["
				+ "rpaToolId=" + rpaToolId
				+ ", scenarioId=" + scenarioId
				+ ", scenarioName=" + scenarioName
				+ ", scenarioIdentifyString=" + scenarioIdentifyString
				+ ", scenarioOperationResultCreateSettingId=" + scenarioOperationResultCreateSettingId
				+ ", ownerRoleId=" + ownerRoleId
				+ "]";
	}

}
