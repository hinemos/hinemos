/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.ArrayList;
import java.util.List;

public class CreatePerfFileResponse {
	public CreatePerfFileResponse(){
	}
	
	private List<String> fileList = new ArrayList<>();
	
	public List<String> getFileList(){
		return this.fileList;
	}
	
	public void setFileList(List<String> fileList){
		this.fileList = fileList;
	}

}
