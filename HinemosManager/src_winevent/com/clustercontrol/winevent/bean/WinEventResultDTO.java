/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
