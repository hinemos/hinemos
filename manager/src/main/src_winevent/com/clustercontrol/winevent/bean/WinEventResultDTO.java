/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.bean;

import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.systemlog.service.MessageInfo;

public class WinEventResultDTO {
	
	public String message;
	public MessageInfo msgInfo;
	public MonitorInfo monitorInfo;
	public MonitorStringValueInfo monitorStrValueInfo;
	public RunInstructionInfo runInstructionInfo;
	
	public WinEventResultDTO() {
		
	}
	
	public WinEventResultDTO(String message, MessageInfo msgInfo, MonitorInfo monitorInfo, MonitorStringValueInfo monitorStrValueInfo, RunInstructionInfo runInstructionInfo) {
		this.message = message;
		this.msgInfo = msgInfo;
		this.monitorInfo = monitorInfo;
		this.monitorStrValueInfo = monitorStrValueInfo;
		this.runInstructionInfo = runInstructionInfo;
	}
	
}
