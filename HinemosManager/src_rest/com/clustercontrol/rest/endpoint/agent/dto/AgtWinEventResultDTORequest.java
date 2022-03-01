/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.winevent.bean.WinEventResultDTO;

@RestBeanConvertAssertion(to = WinEventResultDTO.class)
public class AgtWinEventResultDTORequest extends AgentRequestDto {

	// ---- from WinEventResultDTO
	private String message;
	private AgtMessageInfoRequest msgInfo;
	private AgtMonitorInfoRequest monitorInfo;
	private AgtMonitorStringValueInfoRequest monitorStrValueInfo;
	private AgtRunInstructionInfoRequest runInstructionInfo;

	public AgtWinEventResultDTORequest() {
	}

	// ---- accessors

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
