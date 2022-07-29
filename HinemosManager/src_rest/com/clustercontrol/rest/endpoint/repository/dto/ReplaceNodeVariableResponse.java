/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

public class ReplaceNodeVariableResponse {

	private String replaceStr;

	public ReplaceNodeVariableResponse() {
	}

	public String getReplaceStr() {
		return replaceStr;
	}

	public void setReplaceStr(String replaceStr) {
		this.replaceStr = replaceStr;
	}
}
