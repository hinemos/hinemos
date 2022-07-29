/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.bean;

import java.util.Map;

import javax.xml.bind.annotation.XmlType;

/**
 * 情報収集に利用するコマンドに埋め込む変数情報の格納クラス<BR />
 * 
 * @since 4.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class CommandVariableDTO {

	private String facilityId;
	private Map<String, String> variables;

	public CommandVariableDTO() {

	}

	public CommandVariableDTO(String facilityId, Map<String, String> variables) {
		this.facilityId = facilityId;
		this.variables = variables;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityId() {
		return this.facilityId;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public Map<String, String> getVariables() {
		return this.variables;
	}

	@Override
	public String toString() {
		StringBuilder variableStr = new StringBuilder();
		if (variables != null) {
			for (Map.Entry<String, String> entry : variables.entrySet()) {
				variableStr.append(variableStr.length() == 0 ? "" : ", ");
				variableStr.append(String.format("[key = %s, value = %s]", entry.getKey(), entry.getValue()));
			}
		}

		String ret = this.getClass().getCanonicalName() + " [facilityId = " + facilityId
				+ ", variables = (" + variableStr + ")"
				+ "]";

		return ret;
	}

}
