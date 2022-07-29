/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.Map;

import com.clustercontrol.custom.bean.CommandVariableDTO;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = CommandVariableDTO.class)
public class AgtCustomMonitorVarsInfoResponse {

	// ---- from CommandVariableDTO
	private String facilityId;
	private Map<String, String> variables;

	public AgtCustomMonitorVarsInfoResponse() {
	}

	// ---- accessors

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}
}
