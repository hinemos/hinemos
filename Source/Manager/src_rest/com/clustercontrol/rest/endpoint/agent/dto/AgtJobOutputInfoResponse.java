/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

public class AgtJobOutputInfoResponse {
	/** 出力先 - ディレクトリ */
	private String directory;
	
	/** 出力先 - ファイル名 */
	private String fileName;
	
	/** 追記フラグ */
	private Boolean appendFlg;

	public AgtJobOutputInfoResponse() {
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

	public void setAppendFlg(Boolean appendFlg) {
		this.appendFlg = appendFlg;
	}

	public Boolean getAppendFlg() {
		return appendFlg;
	}
}
