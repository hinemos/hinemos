/*

 Copyright (C) 2011 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.agent.log;

import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * 監視設定と監視結果をまとめるためのクラス
 * 
 */
public class MonitorInfoWrapper {
	public final MonitorInfo monitorInfo;
	public final RunInstructionInfo runInstructionInfo;
	
	public MonitorInfoWrapper(MonitorInfo monitorInfo, RunInstructionInfo runInstructionInfo) {
		this.monitorInfo = monitorInfo;
		this.runInstructionInfo = runInstructionInfo;
	}
	
	public String getId() {
		if (runInstructionInfo == null) {
			return monitorInfo.getMonitorId();
		} else {
			return runInstructionInfo.getSessionId() + runInstructionInfo.getJobunitId()
				+ runInstructionInfo.getJobId() + runInstructionInfo.getFacilityId() + monitorInfo.getMonitorId();
		}
	}
}