/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.util.MessageConstant;

public class RpaLogFileCheckInfoResponse {
	/** 環境毎のRPAツールID */
	@RestItemName(MessageConstant.RPA_TOOL_ID)
	private String rpaToolEnvId;
	/** ディレクトリ */
	@RestItemName(MessageConstant.DIRECTORY)
	private String directory;
	/** ファイル名(正規表現) */
	@RestItemName(MessageConstant.FILE_NAME)
	private String fileName;
	/** ファイルエンコーディング */
	@RestItemName(MessageConstant.FILE_ENCODING)
	private String fileEncoding;

	public RpaLogFileCheckInfoResponse() {
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
	public String getRpaToolEnvId() {
		return rpaToolEnvId;
	}
	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.rpaToolEnvId = rpaToolEnvId;
	}
}