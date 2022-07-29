/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.binary.bean.BinaryResultDTO;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = BinaryResultDTO.class)
public class AgtBinaryResultDTORequest extends AgentRequestDto {

	// ---- fro BinaryResultDTO
	private AgtBinaryFileDTORequest binaryFile;
	private List<AgtBinaryRecordDTORequest> binaryRecords;
	private AgtMessageInfoRequest msgInfo;
	private AgtMonitorInfoRequest monitorInfo;
	private AgtRunInstructionInfoRequest runInstructionInfo;

	public AgtBinaryResultDTORequest() {
	}

	// ---- accessors

	public AgtBinaryFileDTORequest getBinaryFile() {
		return binaryFile;
	}

	public void setBinaryFile(AgtBinaryFileDTORequest binaryFile) {
		this.binaryFile = binaryFile;
	}

	public List<AgtBinaryRecordDTORequest> getBinaryRecords() {
		return binaryRecords;
	}

	public void setBinaryRecords(List<AgtBinaryRecordDTORequest> binaryRecords) {
		this.binaryRecords = binaryRecords;
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

	public AgtRunInstructionInfoRequest getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setRunInstructionInfo(AgtRunInstructionInfoRequest runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

}
