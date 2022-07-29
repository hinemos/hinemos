/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.xcloud.model.RoleRelationEntity;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class RoleRelation {
	private String roleId;
	
	public RoleRelation() {
	}
	
	public RoleRelation(String roleId) {
		this.roleId = roleId;
	}
	
	public RoleRelation(RoleRelationEntity entity) {
		this.roleId = entity.getRoleId();
	}
	
	@ElementId("XCLOUD_CORE_ROLE_ID")
	@Identity
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
}
