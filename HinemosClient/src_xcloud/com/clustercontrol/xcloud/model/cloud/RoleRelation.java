/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;


public class RoleRelation {
	private String id;
	
	public void set(com.clustercontrol.ws.xcloud.RoleRelation roleRelation){
		id = roleRelation.getRoleId();
	}

	public com.clustercontrol.ws.xcloud.RoleRelation getDTO(){
		com.clustercontrol.ws.xcloud.RoleRelation dto = new com.clustercontrol.ws.xcloud.RoleRelation();
		dto.setRoleId(id);
		return dto;
	}
	
	public String getId() {return id;}
}
