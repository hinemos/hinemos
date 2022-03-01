/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import com.clustercontrol.rest.endpoint.access.dto.AddRoleInfoRequest;
import com.clustercontrol.rest.endpoint.access.dto.AssignUserWithRoleRequest;

public class ImportRoleRecordRequest extends AbstractImportRecordRequest<AddRoleInfoRequest>{
	
	AssignUserWithRoleRequest assignUser;

	public ImportRoleRecordRequest(){
	}

	public AssignUserWithRoleRequest getAssignUser() {
		return assignUser;
	}

	public void setAssignUser(AssignUserWithRoleRequest assignUser) {
		this.assignUser = assignUser;
	}
	
}
