/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 監視ジョブのEndNode情報
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class MonitorJobEndNode implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private RunInstructionInfo runInstructionInfo = null;
	private String monitorTypeId = null;
	private String message = null;
	private String errorMessage = null;
	private Integer status = null;
	private Integer endValue = null;

	public MonitorJobEndNode(
			RunInstructionInfo runInstructionInfo,
			String monitorTypeId,
			String message,
			String errorMessage,
			Integer status,
			Integer endValue) {
		super();
		this.runInstructionInfo = runInstructionInfo;
		this.monitorTypeId = monitorTypeId;
		this.message = message;
		this.errorMessage = errorMessage;
		this.status = status;
		this.endValue = endValue;
	}

	public MonitorJobEndNode() {}

	public RunInstructionInfo getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setRunInstructionInfo(RunInstructionInfo runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getEndValue() {
		return endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}
}
