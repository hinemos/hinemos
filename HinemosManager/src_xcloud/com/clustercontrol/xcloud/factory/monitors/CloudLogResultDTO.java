/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.xcloud.factory.monitors;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.systemlog.service.MessageInfo;

@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class CloudLogResultDTO {
	
	public String message;
	public MessageInfo msgInfo;
	public MonitorInfo monitorInfo;
	public MonitorStringValueInfo monitorStrValueInfo;
	public MonitorPluginStringInfo monitorPlgStrCheckInfo;
	public RunInstructionInfo runInstructionInfo;
	public Long lastFireTime;
	
	public CloudLogResultDTO() {
		
	}

	public CloudLogResultDTO(String message, MessageInfo msgInfo, MonitorInfo monitorInfo, MonitorStringValueInfo monitorStrValueInfo, MonitorPluginStringInfo monitorPlgStrCheckInfo, RunInstructionInfo runInstructionInfo, Long lastFireTime) {
		this.message = message;
		this.msgInfo = msgInfo;
		this.monitorInfo = monitorInfo;
		this.monitorStrValueInfo = monitorStrValueInfo;
		this.monitorPlgStrCheckInfo = monitorPlgStrCheckInfo;
		this.runInstructionInfo = runInstructionInfo;
		this.lastFireTime = lastFireTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public MessageInfo getMsgInfo() {
		return msgInfo;
	}

	public void setMsgInfo(MessageInfo msgInfo) {
		this.msgInfo = msgInfo;
	}

	public MonitorInfo getMonitorInfo() {
		return monitorInfo;
	}

	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	public MonitorStringValueInfo getMonitorStrValueInfo() {
		return monitorStrValueInfo;
	}

	public void setMonitorStrValueInfo(MonitorStringValueInfo monitorStrValueInfo) {
		this.monitorStrValueInfo = monitorStrValueInfo;
	}

	public MonitorPluginStringInfo getMonitorPlgStrCheckInfo() {
		return monitorPlgStrCheckInfo;
	}

	public void setMonitorPlgStrCheckInfo(MonitorPluginStringInfo monitorPlgStrCheckInfo) {
		this.monitorPlgStrCheckInfo = monitorPlgStrCheckInfo;
	}

	public RunInstructionInfo getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setRunInstructionInfo(RunInstructionInfo runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	public Long getLastFireTime() {
		return lastFireTime;
	}

	public void setLastFireTime(Long lastFireTime) {
		this.lastFireTime = lastFireTime;
	}
	
}
