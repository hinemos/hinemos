/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.util;

import java.io.File;

public class RestDownloadFile {

	private File tempFile;
	private String fileName;

	public RestDownloadFile(File tempFile, String fileName) {
		this.tempFile = tempFile;
		this.fileName = fileName;
	}

	public File getTempFile() {
		return tempFile;
	}

	public void setTempFile(File tempFile) {
		this.tempFile = tempFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return "RestDownloadFile [tempFile=" + tempFile + ", fileName=" + fileName + "]";
	}

}
