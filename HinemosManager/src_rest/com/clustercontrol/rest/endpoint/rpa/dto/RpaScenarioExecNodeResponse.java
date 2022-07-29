/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rpa.scenario.model.RpaScenarioExecNode;

@RestBeanConvertIdClassSet(infoClass=RpaScenarioExecNode.class,idName="id")
public class RpaScenarioExecNodeResponse {
	
	public RpaScenarioExecNodeResponse() {
	}

	/** ファシリティID*/
	private String facilityId;
	/** ファシリティ名*/
	private String facilityName;
	/** シナリオID */
	private String scenarioId;

	/** ファシリティID */
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	/** ファシリティ名*/
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}
	/** シナリオID */
	public String getScenarioId() {
		return scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	@Override
	public String toString() {
		return "RpaScenarioExecNodeResponse [facilityId=" + facilityId + ", facilityName=" + facilityName + ", scenarioId=" + scenarioId + "]";
	}


}
