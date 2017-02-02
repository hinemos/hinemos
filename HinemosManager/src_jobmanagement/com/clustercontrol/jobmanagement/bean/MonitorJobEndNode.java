/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
