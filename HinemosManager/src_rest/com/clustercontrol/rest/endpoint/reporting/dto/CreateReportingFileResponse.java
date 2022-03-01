/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import java.util.List;

public class CreateReportingFileResponse {
	
	public CreateReportingFileResponse() {
	}

	private List<String> fileNameList;

	public List<String> getFileNameList() {
		return fileNameList;
	}

	public void setFileNameList(List<String> fileNameList) {
		this.fileNameList = fileNameList;
	}

	@Override
	public String toString() {
		return "CreateReportingFileResponse [fileNameList=" + fileNameList + "]";
	}

}
