/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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