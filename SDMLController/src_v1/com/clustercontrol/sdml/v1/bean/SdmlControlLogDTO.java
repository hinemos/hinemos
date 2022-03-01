/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.bean;

/**
 * SDML制御ログ用DTO
 */
public class SdmlControlLogDTO {

	private Long time;
	private String hostname;
	private String applicationId;
	private String pid;
	private String controlCode;
	private String message;
	private String orgLogLine;

	public SdmlControlLogDTO() {
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getControlCode() {
		return controlCode;
	}

	public void setControlCode(String controlCode) {
		this.controlCode = controlCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOrgLogLine() {
		return orgLogLine;
	}

	public void setOrgLogLine(String orgLogLine) {
		this.orgLogLine = orgLogLine;
	}
}
