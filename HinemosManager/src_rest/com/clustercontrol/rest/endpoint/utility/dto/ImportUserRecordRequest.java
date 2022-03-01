/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import com.clustercontrol.rest.endpoint.access.dto.AddUserInfoRequest;

public class ImportUserRecordRequest  extends AbstractImportRecordRequest<AddUserInfoRequest>{
	private String password;
	public ImportUserRecordRequest(){
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
