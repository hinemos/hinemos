/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.systemlog.service.MessageInfo;

@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class BinaryResultDTO {

	public BinaryFileDTO binaryFile;
	public List<BinaryRecordDTO> binaryRecords;
	public MessageInfo msgInfo;
	public MonitorInfo monitorInfo;
	public RunInstructionInfo runInstructionInfo;
	
	public BinaryResultDTO() {
		
	}

	public BinaryResultDTO(BinaryFileDTO binaryFile, List<BinaryRecordDTO> binaryRecords, MessageInfo msgInfo, MonitorInfo monitorInfo, RunInstructionInfo runInstructionInfo) {
		this.binaryFile = binaryFile;
		this.binaryRecords = binaryRecords;
		this.msgInfo = msgInfo;
		this.monitorInfo = monitorInfo;
		this.runInstructionInfo = runInstructionInfo;
	}
	
}
