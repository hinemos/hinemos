/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

public class GetScriptResponse {

	private Boolean empty;
	private String scriptName;
	private String scriptEncoding;
	private String scriptContent;

	public GetScriptResponse() {
	}

	public Boolean getEmpty() {
		return empty;
	}

	public void setEmpty(Boolean empty) {
		this.empty = empty;
	}

	/**
	 * {@link #empty} が true の場合は使用しないでください。
	 */
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	/**
	 * {@link #empty} が true の場合は使用しないでください。
	 */
	public String getScriptEncoding() {
		return scriptEncoding;
	}

	public void setScriptEncoding(String scriptEncoding) {
		this.scriptEncoding = scriptEncoding;
	}

	/**
	 * {@link #empty} が true の場合は使用しないでください。
	 */
	public String getScriptContent() {
		return scriptContent;
	}

	public void setScriptContent(String scriptContent) {
		this.scriptContent = scriptContent;
	}

}
