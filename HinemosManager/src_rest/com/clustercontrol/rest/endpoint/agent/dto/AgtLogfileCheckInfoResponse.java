/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.logfile.model.LogfileCheckInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = LogfileCheckInfo.class)
public class AgtLogfileCheckInfoResponse {

	// LogfileCheckInfo
	private String monitorId;
	private String monitorTypeId;
	private String directory;
	private String fileName;
	private String fileEncoding;
	private String fileReturnCode;
	private String logfile;
	private String patternHead;
	private String patternTail;
	private Integer maxBytes;

	public AgtLogfileCheckInfoResponse() {
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
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

	public String getLogfile() {
		return logfile;
	}

	public void setLogfile(String logfile) {
		this.logfile = logfile;
	}

}
