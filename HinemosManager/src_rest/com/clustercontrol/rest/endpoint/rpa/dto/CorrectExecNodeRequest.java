/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class CorrectExecNodeRequest implements RequestDto {
	/** シナリオ実績作成設定ID */
	@RestValidateString(notNull=true, minLen=1, maxLen=64)
	@RestItemName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID)
	private String scenarioOperationResultCreateSettingId;
	/** シナリオ識別文字列 */
	@RestValidateString(notNull=true, minLen=1, maxLen=512)
	@RestItemName(MessageConstant.RPA_SCENARIO_SCENARIO_IDENTIFY_STRING)
	private String scenarioIdentifyString;
	/** 実行ノード(ファシリティID, シナリオID) */
	@RestValidateObject(notNull=true)
	@RestItemName(MessageConstant.EXECUTION_NODE)
	private List<CorrectExecNodeDetailRequest> execNodes;

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


	/** 実行ノード(ファシリティID, シナリオID) */
	public List<CorrectExecNodeDetailRequest> getExecNodes() {
		return execNodes;
	}

	public void setExecNodes(List<CorrectExecNodeDetailRequest> execNodes) {
		this.execNodes = execNodes;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
