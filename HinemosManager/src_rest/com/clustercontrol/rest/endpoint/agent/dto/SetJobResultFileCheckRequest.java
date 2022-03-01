/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.jobmanagement.bean.RunResultFileCheckInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = RunResultFileCheckInfo.class)
public class SetJobResultFileCheckRequest extends AgentRequestDto {

	// ---- from RunResultFileCheckInfo
	private String directory;
	private String fileName;
	private Integer passedEventType;
	private Long fileTimestamp;
	private Long fileSize;

	public SetJobResultFileCheckRequest() {
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getPassedEventType() {
		return passedEventType;
	}

	public void setPassedEventType(Integer passedEventType) {
		this.passedEventType = passedEventType;
	}

	public Long getFileTimestamp() {
		return fileTimestamp;
	}

	public void setFileTimestamp(Long fileTimestamp) {
		this.fileTimestamp = fileTimestamp;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
}
