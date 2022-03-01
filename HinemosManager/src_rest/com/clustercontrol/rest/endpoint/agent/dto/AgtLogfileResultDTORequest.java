/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = LogfileResultDTO.class)
public class AgtLogfileResultDTORequest extends AgentRequestDto {

	// ---- from LogfileResultDTO
	public String message;
	public AgtMessageInfoRequest msgInfo;
	public AgtMonitorInfoRequest monitorInfo;
	public AgtMonitorStringValueInfoRequest monitorStrValueInfo;
	public AgtRunInstructionInfoRequest runInstructionInfo;

	public AgtLogfileResultDTORequest() {
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public AgtMessageInfoRequest getMsgInfo() {
		return msgInfo;
	}

	public void setMsgInfo(AgtMessageInfoRequest msgInfo) {
		this.msgInfo = msgInfo;
	}

	public AgtMonitorInfoRequest getMonitorInfo() {
		return monitorInfo;
	}

	public void setMonitorInfo(AgtMonitorInfoRequest monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	public AgtMonitorStringValueInfoRequest getMonitorStrValueInfo() {
		return monitorStrValueInfo;
	}

	public void setMonitorStrValueInfo(AgtMonitorStringValueInfoRequest monitorStrValueInfo) {
		this.monitorStrValueInfo = monitorStrValueInfo;
	}

	public AgtRunInstructionInfoRequest getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setRunInstructionInfo(AgtRunInstructionInfoRequest runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

}
