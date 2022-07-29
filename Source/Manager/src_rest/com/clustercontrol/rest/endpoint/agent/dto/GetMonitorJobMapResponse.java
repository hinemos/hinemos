/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class GetMonitorJobMapResponse {
	private List<AgtMonitorInfoResponse> monitorInfoList;
	private List<AgtRunInstructionInfoResponse> runInstructionInfoList;

	public GetMonitorJobMapResponse() {
	}

	public List<AgtMonitorInfoResponse> getMonitorInfoList() {
		return monitorInfoList;
	}

	public void setMonitorInfoList(List<AgtMonitorInfoResponse> monitorInfoList) {
		this.monitorInfoList = monitorInfoList;
	}

	public List<AgtRunInstructionInfoResponse> getRunInstructionInfoList() {
		return runInstructionInfoList;
	}

	public void setRunInstructionInfoList(List<AgtRunInstructionInfoResponse> runInstructionInfoList) {
		this.runInstructionInfoList = runInstructionInfoList;
	}

}
