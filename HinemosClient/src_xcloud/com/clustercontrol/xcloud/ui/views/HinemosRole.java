/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import org.openapitools.client.model.RoleInfoResponse;

import com.clustercontrol.xcloud.model.cloud.IHinemosManager;

public class HinemosRole {
	IHinemosManager manager;
	RoleInfoResponse roleInfo;
	
	public IHinemosManager getManager() {
		return manager;
	}
	public void setManager(IHinemosManager manager) {
		this.manager = manager;
	}
	
	public RoleInfoResponse getRoleInfo() {
		return roleInfo;
	}
	public void setRoleInfo(RoleInfoResponse roleInfo) {
		this.roleInfo = roleInfo;
	}
}
