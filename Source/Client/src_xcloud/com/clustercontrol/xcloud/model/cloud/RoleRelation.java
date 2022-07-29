/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import org.openapitools.client.model.RoleRelationResponse;

public class RoleRelation {
	private String id;
	
	public void set(RoleRelationResponse roleRelation){
		id = roleRelation.getRoleId();
	}

	public RoleRelationResponse getDTO(){
		RoleRelationResponse dto = new RoleRelationResponse();
		dto.setRoleId(id);
		return dto;
	}
	
	public String getId() {return id;}
}
