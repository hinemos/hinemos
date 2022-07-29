/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class CorrectExecNodeDetailRequest implements RequestDto {

	public CorrectExecNodeDetailRequest() {
	}

	/** シナリオID */
	@RestValidateString(notNull=true, minLen=1, maxLen=64)
	@RestItemName(MessageConstant.RPA_SCENARIO_ID)
	private String scenarioId;
	/** ファシリティID*/
	@RestValidateString(notNull=true, minLen=1, maxLen=512)
	@RestItemName(MessageConstant.FACILITY_ID)
	private String facilityId;

	/** シナリオID */
	public String getScenarioId() {
		return scenarioId;
	}
	
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	/** ファシリティID */
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public String toString() {
		return "CorrectExecNodeDetailRequest [scenarioId=" + scenarioId + ", facilityId=" + facilityId + "]";
	}

}
