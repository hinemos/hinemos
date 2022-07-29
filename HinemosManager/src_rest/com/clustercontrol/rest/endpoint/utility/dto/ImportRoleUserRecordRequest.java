/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import com.clustercontrol.rest.endpoint.access.dto.AssignUserWithRoleRequest;

public class ImportRoleUserRecordRequest extends AbstractImportRecordRequest<AssignUserWithRoleRequest>{
	
	private String roleId;
	
	public ImportRoleUserRecordRequest(){
		
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	
	
}
