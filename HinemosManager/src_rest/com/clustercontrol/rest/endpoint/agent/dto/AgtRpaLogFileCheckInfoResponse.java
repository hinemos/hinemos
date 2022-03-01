/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.util.MessageConstant;

public class AgtRpaLogFileCheckInfoResponse {
	/** ディレクトリ */
	@RestItemName(MessageConstant.DIRECTORY)
	private String directory;
	/** ファイル名(正規表現) */
	@RestItemName(MessageConstant.FILE_NAME)
	private String fileName;
	/** ファイルエンコーディング */
	@RestItemName(MessageConstant.FILE_ENCODING)
	private String fileEncoding;

	public AgtRpaLogFileCheckInfoResponse() {
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
}