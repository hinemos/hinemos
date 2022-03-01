/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class LogfileCheckInfoRequest implements RequestDto {
	private String directory;
	private String fileName;
	private String fileEncoding;
	private String fileReturnCode;
	private String patternHead; 
	private String patternTail; 
	private Integer maxBytes;  
	public LogfileCheckInfoRequest() {
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
	public String getFileEncoding() {
		return fileEncoding;
	}
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}
	public String getFileReturnCode() {
		return fileReturnCode;
	}
	public void setFileReturnCode(String fileReturnCode) {
		this.fileReturnCode = fileReturnCode;
	}
	public String getPatternHead() {
		return patternHead;
	}
	public void setPatternHead(String patternHead) {
		this.patternHead = patternHead;
	}
	public String getPatternTail() {
		return patternTail;
	}
	public void setPatternTail(String patternTail) {
		this.patternTail = patternTail;
	}
	public Integer getMaxBytes() {
		return maxBytes;
	}
	public void setMaxBytes(Integer maxBytes) {
		this.maxBytes = maxBytes;
	}
	@Override
	public String toString() {
		return "LogfileCheckInfo [directory=" + directory + ", fileName=" + fileName
				+ ", fileEncoding=" + fileEncoding + ", fileReturnCode=" + fileReturnCode + ", patternHead="
				+ patternHead + ", patternTail=" + patternTail + ", maxBytes=" + maxBytes + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}