/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyCloudLoginUserRoleRelationRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_ROLERELATIONS)
	@RestValidateObject(notNull = true)
	private List<RoleRelationRequest> roleRelations;
	
	public ModifyCloudLoginUserRoleRelationRequest() {
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
	
	public List<RoleRelationRequest> getRoleRelations() {
		return roleRelations;
	}
	public void setRoleRelations(List<RoleRelationRequest> roleRelations) {
		this.roleRelations = roleRelations;
	}
}
