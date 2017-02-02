/*

Copyright (C) 2006 NTT DATA Corporation

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

import com.clustercontrol.notify.bean.OutputBasicInfo;


/**
 * ジョブセッションを作成するためのメッセージ
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobSessionRequestMessage implements Serializable {
	private static final long serialVersionUID = -2188950801974373408L;

	private String sessionId;
	private String jobunitId;
	private String jobId;
	private OutputBasicInfo outputBasicInfo;
	private JobTriggerInfo triggerInfo;
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public OutputBasicInfo getOutputBasicInfo() {
		return outputBasicInfo;
	}

	public void setOutputBasicInfo(OutputBasicInfo outputBasicInfo) {
		this.outputBasicInfo = outputBasicInfo;
	}

	public JobTriggerInfo getTriggerInfo() {
		return triggerInfo;
	}

	public void setTriggerInfo(JobTriggerInfo triggerInfo) {
		this.triggerInfo = triggerInfo;
	}

	// for cluster jax-ws
	public JobSessionRequestMessage() { }

	public JobSessionRequestMessage(
			String sessionId,
			String jobunitId,
			String jobId,
			OutputBasicInfo outputBasicInfo,
			JobTriggerInfo triggerInfo
			) {
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.outputBasicInfo = outputBasicInfo;
		this.triggerInfo = triggerInfo;
	}

	@Override
	public String toString(){
		String str = "sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId;
		return str;
	}
}
