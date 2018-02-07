/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.notify.bean.OutputBasicInfo;


@XmlType(namespace = "http://agent.ws.clustercontrol.com")
public class AgentOutputBasicInfo {
	OutputBasicInfo outputBasicInfo = null;
	AgentInfo agentInfo = null;
	public OutputBasicInfo getOutputBasicInfo() {
		return outputBasicInfo;
	}
	public void setOutputBasicInfo(OutputBasicInfo outputBasicInfo) {
		this.outputBasicInfo = outputBasicInfo;
	}
	public AgentInfo getAgentInfo() {
		return agentInfo;
	}
	public void setAgentInfo(AgentInfo agentInfo) {
		this.agentInfo = agentInfo;
	}
}
