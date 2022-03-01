/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class GetRpaScenarioListRequest implements RequestDto {
	
	public GetRpaScenarioListRequest (){}
	
	/** RPAツールID */
	private String rpaToolId;
	/** RPAシナリオID */
	private String scenarioId;
	/** RPAシナリオ名 */
	private String scenarioName;
	/** RPAシナリオ識別文字列 */
	private String scenarioIdentifyString;
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** オーナーロールID */
	private String ownerRoleId;

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
		return "GetRpaScenarioRequest [rpaToolId=" + rpaToolId + ", scenarioId=" + scenarioId
				+ ", scenarioName=" + scenarioName + ", scenarioIdentifyString=" + scenarioIdentifyString 
				+ ", scenarioOperationResultCreateSettingId=" + scenarioOperationResultCreateSettingId 
				+ ", ownerRoleId=" + ownerRoleId + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
