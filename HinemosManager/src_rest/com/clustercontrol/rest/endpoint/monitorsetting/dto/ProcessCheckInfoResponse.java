/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ProcessCheckInfoResponse {
	private String command;
	private String param;
	private Boolean caseSensitivityFlg;

	public ProcessCheckInfoResponse() {
	}

	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}
	@Override
	public String toString() {
		return "ProcessCheckInfo [command=" + command
				+ ", param=" + param + ", caseSensitivityFlg=" + caseSensitivityFlg + "]";
	}

}